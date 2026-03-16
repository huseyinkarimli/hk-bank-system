package az.hkbank.module.transaction.dto;

import az.hkbank.module.account.entity.CurrencyType;
import az.hkbank.module.transaction.entity.TransactionStatus;
import az.hkbank.module.transaction.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for transaction information responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private String referenceNumber;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal amount;
    private BigDecimal convertedAmount;
    private BigDecimal exchangeRate;
    private CurrencyType sourceCurrency;
    private CurrencyType targetCurrency;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private String description;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
