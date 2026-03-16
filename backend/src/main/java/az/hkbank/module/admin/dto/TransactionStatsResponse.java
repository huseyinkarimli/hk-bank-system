package az.hkbank.module.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response DTO for transaction statistics.
 * Contains aggregated transaction data for admin dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatsResponse {

    private Long totalTransactions;
    private BigDecimal totalVolume;
    private Map<String, Long> countByStatus;
    private Map<String, BigDecimal> dailyVolume;
}
