package az.hkbank.module.account.dto;

import az.hkbank.module.payment.dto.PaymentSummaryResponse;
import az.hkbank.module.transaction.dto.TransactionSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO containing complete statement data for an account.
 * Includes account details, transactions, payments, and balance summaries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementData {

    private AccountResponse account;
    private List<TransactionSummaryResponse> transactions;
    private List<PaymentSummaryResponse> payments;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal totalCredit;
    private BigDecimal totalDebit;
    private LocalDateTime periodFrom;
    private LocalDateTime periodTo;
}
