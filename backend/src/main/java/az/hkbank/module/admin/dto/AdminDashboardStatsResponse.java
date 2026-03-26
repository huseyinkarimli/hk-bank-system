package az.hkbank.module.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Aggregated metrics for the admin dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsResponse {

    private long totalUsers;
    private long bannedUsers;
    private long totalActiveCards;
    private long totalBlockedCards;
    private long todayTransactionCount;
    private BigDecimal todayTransactionVolume;
    private BigDecimal totalBalanceInAzn;
    private LocalDateTime generatedAt;
}
