package az.hkbank.module.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for system information.
 * Contains system metrics and statistics for admin monitoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemInfoResponse {

    private Long jvmMemoryUsed;
    private Long jvmMemoryMax;
    private Integer dbActiveConnections;
    private Long totalUsers;
    private Long totalTransactions;
    private BigDecimal totalTransactionVolume;
    private String appUptime;
    private String javaVersion;
    private String springBootVersion;
    private LocalDateTime timestamp;
}
