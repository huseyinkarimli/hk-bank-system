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
 * DTO for lightweight transaction summary responses.
 * Used in list operations to reduce data transfer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummaryResponse {

    private Long id;
    private String referenceNumber;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal amount;
    private CurrencyType sourceCurrency;
    private LocalDateTime createdAt;
}
