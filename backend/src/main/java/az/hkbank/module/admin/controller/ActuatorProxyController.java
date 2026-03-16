package az.hkbank.module.admin.controller;

import az.hkbank.common.response.ApiResponse;
import az.hkbank.module.admin.dto.SystemInfoResponse;
import az.hkbank.module.transaction.entity.Transaction;
import az.hkbank.module.transaction.entity.TransactionStatus;
import az.hkbank.module.transaction.repository.TransactionRepository;
import az.hkbank.module.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST controller for system monitoring and actuator endpoints.
 * Provides health, metrics, and system information for admin monitoring.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "System Monitoring", description = "System health and metrics endpoints for administrators")
public class ActuatorProxyController {

    private final HealthEndpoint healthEndpoint;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final DataSource dataSource;

    @Autowired(required = false)
    private MetricsEndpoint metricsEndpoint;

    /**
     * Proxies the actuator health endpoint.
     *
     * @return health status
     */
    @GetMapping("/health")
    @Operation(summary = "Get system health", description = "Retrieves application health status")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Health status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<HealthComponent>> getHealth() {
        log.info("Admin fetching system health");

        HealthComponent health = healthEndpoint.health();

        return ResponseEntity.ok(ApiResponse.success(health));
    }

    /**
     * Proxies the actuator metrics endpoint.
     *
     * @return available metrics
     */
    @GetMapping("/metrics")
    @Operation(summary = "Get system metrics", description = "Retrieves available system metrics")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Metrics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMetrics() {
        log.info("Admin fetching system metrics");

        if (metricsEndpoint == null) {
            Map<String, Object> metricsData = Map.of(
                    "message", "Metrics endpoint not available",
                    "availableMetrics", List.of()
            );
            return ResponseEntity.ok(ApiResponse.success(metricsData));
        }

        Set<String> metricNames = metricsEndpoint.listNames().getNames();

        Map<String, Object> metricsData = Map.of(
                "availableMetrics", metricNames
        );

        return ResponseEntity.ok(ApiResponse.success(metricsData));
    }

    /**
     * Retrieves comprehensive system information.
     *
     * @return system information
     */
    @GetMapping("/info")
    @Operation(summary = "Get system information", description = "Retrieves comprehensive system statistics and information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "System information retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<SystemInfoResponse>> getSystemInfo() {
        log.info("Admin fetching system information");

        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long memoryMax = runtime.maxMemory() / (1024 * 1024);

        int dbConnections = getActiveDbConnections();

        long totalUsers = userRepository.count();
        long totalTransactions = transactionRepository.count();

        List<Transaction> successfulTransactions = transactionRepository.findAll().stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
                .toList();

        BigDecimal totalVolume = successfulTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        String uptime = formatUptime(uptimeMillis);

        String javaVersion = System.getProperty("java.version");
        String springBootVersion = SpringBootVersion.getVersion();

        SystemInfoResponse systemInfo = SystemInfoResponse.builder()
                .jvmMemoryUsed(memoryUsed)
                .jvmMemoryMax(memoryMax)
                .dbActiveConnections(dbConnections)
                .totalUsers(totalUsers)
                .totalTransactions(totalTransactions)
                .totalTransactionVolume(totalVolume)
                .appUptime(uptime)
                .javaVersion(javaVersion)
                .springBootVersion(springBootVersion)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(ApiResponse.success(systemInfo));
    }

    private int getActiveDbConnections() {
        try (Connection connection = dataSource.getConnection()) {
            return 1;
        } catch (Exception e) {
            log.error("Failed to get DB connection count", e);
            return 0;
        }
    }

    private String formatUptime(long uptimeMillis) {
        Duration duration = Duration.ofMillis(uptimeMillis);
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }
}
