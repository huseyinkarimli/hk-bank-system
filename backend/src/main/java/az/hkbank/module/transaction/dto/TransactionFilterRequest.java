package az.hkbank.module.transaction.dto;

import az.hkbank.module.transaction.entity.TransactionStatus;
import az.hkbank.module.transaction.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for filtering transaction queries.
 * Supports date range, type, and status filtering with pagination.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFilterRequest {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
}
