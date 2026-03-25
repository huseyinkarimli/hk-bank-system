package az.hkbank.module.account.service.impl;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.account.dto.AccountResponse;
import az.hkbank.module.account.dto.StatementData;
import az.hkbank.module.account.entity.Account;
import az.hkbank.module.account.mapper.AccountMapper;
import az.hkbank.module.account.repository.AccountRepository;
import az.hkbank.module.account.service.StatementService;
import az.hkbank.module.payment.dto.PaymentSummaryResponse;
import az.hkbank.module.payment.entity.Payment;
import az.hkbank.module.payment.entity.PaymentStatus;
import az.hkbank.module.payment.mapper.PaymentMapper;
import az.hkbank.module.payment.repository.PaymentRepository;
import az.hkbank.module.transaction.dto.TransactionSummaryResponse;
import az.hkbank.module.transaction.entity.Transaction;
import az.hkbank.module.transaction.entity.TransactionStatus;
import az.hkbank.module.transaction.mapper.TransactionMapper;
import az.hkbank.module.transaction.repository.TransactionRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of StatementService interface.
 * Handles account statement generation in JSON and PDF formats.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional(readOnly = true)
    public StatementData getStatement(Long accountId, Long userId, LocalDateTime from, LocalDateTime to) {
        log.info("Generating statement for account: {}, period: {} to {}", accountId, from, to);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.getUser().getId().equals(userId)) {
            throw new BankException(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS, "Account does not belong to user");
        }

        List<Transaction> sentTransactions = transactionRepository.findBySenderAccountIdAndCreatedAtBetween(
                accountId, from, to);
        List<Transaction> receivedTransactions = transactionRepository.findByReceiverAccountIdAndCreatedAtBetween(
                accountId, from, to);

        List<Payment> payments = paymentRepository.findByAccountId(accountId).stream()
                .filter(p -> p.getCreatedAt().isAfter(from) && p.getCreatedAt().isBefore(to))
                .toList();

        List<TransactionSummaryResponse> transactionSummaries = new ArrayList<>();
        transactionSummaries.addAll(sentTransactions.stream()
                .map(transactionMapper::toTransactionSummaryResponse)
                .toList());
        transactionSummaries.addAll(receivedTransactions.stream()
                .map(transactionMapper::toTransactionSummaryResponse)
                .toList());

        List<PaymentSummaryResponse> paymentSummaries = payments.stream()
                .map(paymentMapper::toPaymentSummaryResponse)
                .toList();

        BigDecimal totalCredit = receivedTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
                .map(Transaction::getConvertedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebit = sentTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalDebit = totalDebit.add(payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        BigDecimal closingBalance = account.getBalance();
        BigDecimal openingBalance = closingBalance.subtract(totalCredit).add(totalDebit);

        AccountResponse accountResponse = accountMapper.toAccountResponse(account);

        return StatementData.builder()
                .account(accountResponse)
                .transactions(transactionSummaries)
                .payments(paymentSummaries)
                .openingBalance(openingBalance)
                .closingBalance(closingBalance)
                .totalCredit(totalCredit)
                .totalDebit(totalDebit)
                .periodFrom(from)
                .periodTo(to)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdfStatement(Long accountId, Long userId, LocalDateTime from, LocalDateTime to) {
        log.info("Generating PDF statement for account: {}", accountId);

        try {
            StatementData data = getStatement(accountId, userId, from, to);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            DeviceRgb primaryColor = new DeviceRgb(0, 102, 204);

            Paragraph header = new Paragraph("HK BANK")
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(primaryColor)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("Hesab Çıxarışı / Account Statement")
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(subHeader);

            Table accountTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);

            addInfoRow(accountTable, "Hesab nömrəsi:", data.getAccount().getAccountNumber());
            addInfoRow(accountTable, "IBAN:", data.getAccount().getIban());
            addInfoRow(accountTable, "Valyuta:", data.getAccount().getCurrencyType().toString());
            addInfoRow(accountTable, "Dövr:", 
                    from.format(DATE_FORMATTER) + " - " + to.format(DATE_FORMATTER));

            document.add(accountTable);

            Paragraph balanceHeader = new Paragraph("Balans məlumatı")
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(10)
                    .setMarginBottom(5);
            document.add(balanceHeader);

            Table balanceTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);

            addBalanceRow(balanceTable, "Açılış balansı:", data.getOpeningBalance(), data.getAccount().getCurrencyType().toString());
            addBalanceRow(balanceTable, "Mədaxil (Kredit):", data.getTotalCredit(), data.getAccount().getCurrencyType().toString());
            addBalanceRow(balanceTable, "Məxaric (Debet):", data.getTotalDebit(), data.getAccount().getCurrencyType().toString());
            addBalanceRow(balanceTable, "Bağlanış balansı:", data.getClosingBalance(), data.getAccount().getCurrencyType().toString());

            document.add(balanceTable);

            if (!data.getTransactions().isEmpty()) {
                Paragraph transactionHeader = new Paragraph("Köçürmələr")
                        .setFontSize(12)
                        .setBold()
                        .setMarginTop(10)
                        .setMarginBottom(5);
                document.add(transactionHeader);

                Table transactionTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 2}))
                        .useAllAvailableWidth()
                        .setMarginBottom(10);

                addTableHeader(transactionTable, "Tarix", "Təsvir", "Məbləğ", "Status");

                for (TransactionSummaryResponse txn : data.getTransactions()) {
                    transactionTable.addCell(createCell(txn.getCreatedAt().format(DATE_TIME_FORMATTER)));
                    transactionTable.addCell(createCell(txn.getType().toString()));
                    transactionTable.addCell(createCell(txn.getAmount() + " " + txn.getSourceCurrency()));
                    transactionTable.addCell(createCell(txn.getStatus().toString()));
                }

                document.add(transactionTable);
            }

            if (!data.getPayments().isEmpty()) {
                Paragraph paymentHeader = new Paragraph("Ödənişlər")
                        .setFontSize(12)
                        .setBold()
                        .setMarginTop(10)
                        .setMarginBottom(5);
                document.add(paymentHeader);

                Table paymentTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 2}))
                        .useAllAvailableWidth()
                        .setMarginBottom(10);

                addTableHeader(paymentTable, "Tarix", "Provayder", "Məbləğ", "Status");

                for (PaymentSummaryResponse payment : data.getPayments()) {
                    paymentTable.addCell(createCell(payment.getCreatedAt().format(DATE_TIME_FORMATTER)));
                    paymentTable.addCell(createCell(payment.getProviderName()));
                    paymentTable.addCell(createCell(payment.getAmount() + " AZN"));
                    paymentTable.addCell(createCell(payment.getStatus().toString()));
                }

                document.add(paymentTable);
            }

            Paragraph footer = new Paragraph("Çıxarış tarixi: " + LocalDateTime.now().format(DATE_TIME_FORMATTER))
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(20)
                    .setFontColor(ColorConstants.GRAY);
            document.add(footer);

            document.close();

            log.info("PDF statement generated successfully for account: {}", accountId);

            return baos.toByteArray();

        } catch (BankException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate PDF statement for account: {}", accountId, e);
            throw new BankException(ErrorCode.STATEMENT_GENERATION_FAILED, e.getMessage());
        }
    }

    private void addInfoRow(Table table, String label, String value) {
        table.addCell(createCell(label).setBold());
        table.addCell(createCell(value));
    }

    private void addBalanceRow(Table table, String label, BigDecimal value, String currency) {
        table.addCell(createCell(label).setBold());
        table.addCell(createCell(value + " " + currency));
    }

    private void addTableHeader(Table table, String... headers) {
        DeviceRgb headerColor = new DeviceRgb(240, 240, 240);
        for (String header : headers) {
            table.addHeaderCell(createCell(header)
                    .setBold()
                    .setBackgroundColor(headerColor)
                    .setTextAlignment(TextAlignment.CENTER));
        }
    }

    private Cell createCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setPadding(5)
                .setBorder(Border.NO_BORDER);
    }
}
