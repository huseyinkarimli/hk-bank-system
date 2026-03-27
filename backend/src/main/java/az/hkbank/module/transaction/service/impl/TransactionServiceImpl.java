package az.hkbank.module.transaction.service.impl;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.account.dto.AccountResponse;
import az.hkbank.module.account.entity.Account;
import az.hkbank.module.account.entity.AccountStatus;
import az.hkbank.module.account.mapper.AccountMapper;
import az.hkbank.module.account.repository.AccountRepository;
import az.hkbank.module.admin.dto.AdminFundRequest;
import az.hkbank.module.audit.service.AuditAction;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.card.entity.Card;
import az.hkbank.module.card.entity.CardStatus;
import az.hkbank.module.card.repository.CardRepository;
import az.hkbank.module.notification.entity.NotificationType;
import az.hkbank.module.notification.service.NotificationService;
import az.hkbank.module.transaction.dto.*;
import az.hkbank.module.transaction.entity.Transaction;
import az.hkbank.module.transaction.entity.TransactionStatus;
import az.hkbank.module.transaction.entity.TransactionType;
import az.hkbank.module.transaction.mapper.TransactionMapper;
import az.hkbank.module.transaction.repository.TransactionRepository;
import az.hkbank.module.transaction.service.ExchangeRateService;
import az.hkbank.module.transaction.service.FraudDetectionService;
import az.hkbank.module.transaction.service.TransactionLimitService;
import az.hkbank.module.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Implementation of TransactionService interface.
 * Handles money transfers with fraud detection, limits, and currency conversion.
 * CRITICAL: Uses pessimistic locking to prevent race conditions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final String ACTION_TRANSFER_SUCCESS = "TRANSFER_SUCCESS";
    private static final String ACTION_TRANSFER_FAILED = "TRANSFER_FAILED";

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final TransactionMapper transactionMapper;
    private final ExchangeRateService exchangeRateService;
    private final FraudDetectionService fraudDetectionService;
    private final TransactionLimitService transactionLimitService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final AccountMapper accountMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TransactionResponse transferByCard(Long userId, P2PCardTransferRequest request, String ipAddress) {
        log.info("Processing card transfer: {} -> {}, amount: {}",
                request.getSourceCardNumber(), request.getTargetCardNumber(), request.getAmount());

        Card sourceCard = cardRepository.findByCardNumber(request.getSourceCardNumber())
                .orElseThrow(() -> new BankException(ErrorCode.CARD_NOT_FOUND, "Source card not found"));

        if (!sourceCard.getAccount().getUser().getId().equals(userId)) {
            throw new BankException(ErrorCode.UNAUTHORIZED_CARD_ACCESS, "Source card does not belong to user");
        }

        if (sourceCard.getStatus() == CardStatus.BLOCKED) {
            throw new BankException(ErrorCode.CARD_BLOCKED, "Source card is blocked");
        }

        if (sourceCard.getStatus() == CardStatus.FROZEN) {
            throw new BankException(ErrorCode.CARD_FROZEN, "Source card is frozen");
        }

        Card targetCard = cardRepository.findByCardNumber(request.getTargetCardNumber())
                .orElseThrow(() -> new BankException(ErrorCode.CARD_NOT_FOUND, "Target card not found"));

        if (targetCard.getStatus() != CardStatus.ACTIVE) {
            throw new BankException(ErrorCode.CARD_BLOCKED, "Target card is not active");
        }

        fraudDetectionService.checkForFraud(sourceCard.getId(), userId, ipAddress);

        Account senderAccount = sourceCard.getAccount();
        Account receiverAccount = targetCard.getAccount();

        if (senderAccount.getId().equals(receiverAccount.getId())) {
            throw new BankException(ErrorCode.SAME_ACCOUNT_TRANSFER,
                    "Cannot transfer to the same account");
        }

        transactionLimitService.checkTransactionLimit(userId, request.getAmount(), senderAccount.getCurrencyType());

        return executeTransfer(
                senderAccount,
                receiverAccount,
                sourceCard,
                targetCard,
                request.getAmount(),
                request.getDescription(),
                TransactionType.P2P_CARD,
                userId,
                ipAddress
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TransactionResponse transferByIban(Long userId, P2PIbanTransferRequest request, String ipAddress) {
        log.info("Processing IBAN transfer: {} -> {}, amount: {}",
                request.getSourceIban(), request.getTargetIban(), request.getAmount());

        Account senderAccount = accountRepository.findByIban(request.getSourceIban())
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND, "Source account not found"));

        if (!senderAccount.getUser().getId().equals(userId)) {
            throw new BankException(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS, "Source account does not belong to user");
        }

        if (senderAccount.getStatus() == AccountStatus.BLOCKED) {
            throw new BankException(ErrorCode.ACCOUNT_BLOCKED, "Source account is blocked");
        }

        if (senderAccount.getStatus() == AccountStatus.CLOSED) {
            throw new BankException(ErrorCode.ACCOUNT_CLOSED, "Source account is closed");
        }

        Account receiverAccount = accountRepository.findByIban(request.getTargetIban())
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND, "Target account not found"));

        if (receiverAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BankException(ErrorCode.ACCOUNT_BLOCKED, "Target account is not active");
        }

        if (senderAccount.getId().equals(receiverAccount.getId())) {
            throw new BankException(ErrorCode.SAME_ACCOUNT_TRANSFER,
                    "Cannot transfer to the same account");
        }

        transactionLimitService.checkTransactionLimit(userId, request.getAmount(), senderAccount.getCurrencyType());

        return executeTransfer(
                senderAccount,
                receiverAccount,
                null,
                null,
                request.getAmount(),
                request.getDescription(),
                TransactionType.P2P_IBAN,
                userId,
                ipAddress
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id, Long userId) {
        log.info("Fetching transaction: {} for user: {}", id, userId);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new BankException(ErrorCode.TRANSACTION_NOT_FOUND));

        boolean asSender = transaction.getSenderAccount() != null
                && transaction.getSenderAccount().getUser().getId().equals(userId);
        boolean asReceiver = transaction.getReceiverAccount() != null
                && transaction.getReceiverAccount().getUser().getId().equals(userId);
        if (!asSender && !asReceiver) {
            throw new BankException(ErrorCode.FORBIDDEN, "Unauthorized access to transaction");
        }

        return transactionMapper.toTransactionResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionSummaryResponse> getUserTransactions(Long userId, TransactionFilterRequest filter, Pageable pageable) {
        log.info("Fetching transactions for user: {} with filters", userId);

        Page<Transaction> transactions = transactionRepository.findByUserIdWithFilters(
                userId,
                filter.getType(),
                filter.getStatus(),
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getMinAmount(),
                filter.getMaxAmount(),
                pageable
        );

        return transactions.map(transactionMapper::toTransactionSummaryResponse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountResponse adminDeposit(Long accountId, AdminFundRequest request, String ipAddress) {
        log.info("Admin deposit to account {} amount {}", accountId, request.getAmount());

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (account.isDeleted()) {
            throw new BankException(ErrorCode.ACCOUNT_NOT_FOUND, "Account is deleted");
        }

        account.setBalance(account.getBalance().add(request.getAmount()));
        Account savedAccount = accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .amount(request.getAmount())
                .convertedAmount(request.getAmount())
                .sourceCurrency(account.getCurrencyType())
                .targetCurrency(account.getCurrencyType())
                .senderAccount(null)
                .receiverAccount(savedAccount)
                .description("Admin deposit: " + request.getDescription())
                .ipAddress(ipAddress)
                .completedAt(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        auditService.log(
                savedAccount.getUser().getId(),
                AuditAction.ADMIN_DEPOSIT,
                "Admin deposit " + request.getAmount() + " to account " + savedAccount.getId()
                        + " - " + savedTransaction.getReferenceNumber(),
                ipAddress
        );

        return accountMapper.toAccountResponse(savedAccount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountResponse adminWithdraw(Long accountId, AdminFundRequest request, String ipAddress) {
        log.info("Admin withdraw from account {} amount {}", accountId, request.getAmount());

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (account.isDeleted()) {
            throw new BankException(ErrorCode.ACCOUNT_NOT_FOUND, "Account is deleted");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BankException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        Account savedAccount = accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCESS)
                .amount(request.getAmount())
                .convertedAmount(request.getAmount())
                .sourceCurrency(account.getCurrencyType())
                .targetCurrency(account.getCurrencyType())
                .senderAccount(savedAccount)
                .receiverAccount(null)
                .description("Admin withdrawal: " + request.getDescription())
                .ipAddress(ipAddress)
                .completedAt(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        auditService.log(
                savedAccount.getUser().getId(),
                AuditAction.ADMIN_WITHDRAWAL,
                "Admin withdrawal " + request.getAmount() + " from account " + savedAccount.getId()
                        + " - " + savedTransaction.getReferenceNumber(),
                ipAddress
        );

        return accountMapper.toAccountResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String referenceNumber) {
        log.info("Fetching transaction by reference: {}", referenceNumber);

        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new BankException(ErrorCode.TRANSACTION_NOT_FOUND));

        return transactionMapper.toTransactionResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionSummaryResponse> getAllTransactions(Pageable pageable) {
        log.info("Fetching all transactions (admin)");

        Page<Transaction> transactions = transactionRepository.findAll(pageable);

        return transactions.map(transactionMapper::toTransactionSummaryResponse);
    }

    /**
     * Executes the actual money transfer with pessimistic locking.
     * CRITICAL: Locks accounts in consistent order (lower ID first) to prevent deadlock.
     *
     * @param senderAccount the sender account
     * @param receiverAccount the receiver account
     * @param senderCard the sender card (nullable)
     * @param receiverCard the receiver card (nullable)
     * @param amount the transfer amount
     * @param description the transfer description
     * @param type the transaction type
     * @param userId the user ID
     * @param ipAddress the IP address
     * @return transaction response
     */
    private TransactionResponse executeTransfer(
            Account senderAccount,
            Account receiverAccount,
            Card senderCard,
            Card receiverCard,
            BigDecimal amount,
            String description,
            TransactionType type,
            Long userId,
            String ipAddress) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException(ErrorCode.INVALID_TRANSACTION_AMOUNT,
                    "Amount must be greater than zero");
        }

        Transaction transaction = Transaction.builder()
                .type(type)
                .status(TransactionStatus.PENDING)
                .amount(amount)
                .sourceCurrency(senderAccount.getCurrencyType())
                .targetCurrency(receiverAccount.getCurrencyType())
                .senderAccount(senderAccount)
                .receiverAccount(receiverAccount)
                .senderCard(senderCard)
                .receiverCard(receiverCard)
                .description(description)
                .ipAddress(ipAddress)
                .build();

        try {
            Account lockedSender;
            Account lockedReceiver;

            if (senderAccount.getId() < receiverAccount.getId()) {
                lockedSender = accountRepository.findByIdForUpdate(senderAccount.getId())
                        .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND, "Sender account not found"));
                lockedReceiver = accountRepository.findByIdForUpdate(receiverAccount.getId())
                        .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND, "Receiver account not found"));
            } else {
                lockedReceiver = accountRepository.findByIdForUpdate(receiverAccount.getId())
                        .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND, "Receiver account not found"));
                lockedSender = accountRepository.findByIdForUpdate(senderAccount.getId())
                        .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND, "Sender account not found"));
            }

            if (lockedSender.getBalance().compareTo(amount) < 0) {
                transaction.setStatus(TransactionStatus.REJECTED);
                transaction.setFailureReason("Insufficient balance");
                transactionRepository.save(transaction);

                auditService.log(userId, ACTION_TRANSFER_FAILED,
                        "Transfer rejected: Insufficient balance - " + transaction.getReferenceNumber(),
                        ipAddress);

                throw new BankException(ErrorCode.INSUFFICIENT_BALANCE);
            }

            BigDecimal convertedAmount;
            BigDecimal exchangeRate = null;

            if (senderAccount.getCurrencyType() == receiverAccount.getCurrencyType()) {
                convertedAmount = amount;
            } else {
                exchangeRate = exchangeRateService.getExchangeRate(
                        senderAccount.getCurrencyType(),
                        receiverAccount.getCurrencyType()
                );
                convertedAmount = exchangeRateService.convert(
                        amount,
                        senderAccount.getCurrencyType(),
                        receiverAccount.getCurrencyType()
                );
                transaction.setExchangeRate(exchangeRate);
            }

            transaction.setConvertedAmount(convertedAmount);

            lockedSender.setBalance(lockedSender.getBalance().subtract(amount));
            lockedReceiver.setBalance(lockedReceiver.getBalance().add(convertedAmount));

            accountRepository.save(lockedSender);
            accountRepository.save(lockedReceiver);

            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setCompletedAt(LocalDateTime.now());

            Transaction savedTransaction = transactionRepository.save(transaction);

            auditService.log(userId, ACTION_TRANSFER_SUCCESS,
                    "Transfer completed: " + amount + " " + senderAccount.getCurrencyType() +
                            " -> " + convertedAmount + " " + receiverAccount.getCurrencyType() +
                            " - " + savedTransaction.getReferenceNumber(),
                    ipAddress);

            notificationService.createNotification(
                    userId,
                    NotificationType.TRANSACTION,
                    "Köçürmə uğurla tamamlandı",
                    "Köçürmə uğurla tamamlandı: " + amount + " " + senderAccount.getCurrencyType()
            );

            log.info("Transfer completed successfully: {}", savedTransaction.getReferenceNumber());

            return transactionMapper.toTransactionResponse(savedTransaction);

        } catch (BankException e) {
            if (transaction.getStatus() != TransactionStatus.REJECTED) {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailureReason(e.getErrorCode().getMessage() +
                        (e.getDetail() != null ? ": " + e.getDetail() : ""));
                transactionRepository.save(transaction);

                auditService.log(userId, ACTION_TRANSFER_FAILED,
                        "Transfer failed: " + e.getErrorCode().getMessage() + " - " + transaction.getReferenceNumber(),
                        ipAddress);
            }

            throw e;

        } catch (Exception e) {
            log.error("Unexpected error during transfer", e);

            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Internal error: " + e.getMessage());
            transactionRepository.save(transaction);

            auditService.log(userId, ACTION_TRANSFER_FAILED,
                    "Transfer failed: Internal error - " + transaction.getReferenceNumber(),
                    ipAddress);

            throw new BankException(ErrorCode.TRANSACTION_FAILED, e.getMessage());
        }
    }
}
