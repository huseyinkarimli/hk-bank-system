package az.hkbank.config;

import az.hkbank.module.audit.service.AuditAction;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuration for scheduled tasks.
 * Handles periodic system maintenance and monitoring operations.
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledTasksConfig {

    private final AuditService auditService;
    private final UserRepository userRepository;

    /**
     * Daily task to reset transaction limit counters.
     * Runs at midnight (00:00:00) every day.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyLimits() {
        log.info("Starting daily limit reset task");

        try {
            auditService.log(
                    0L,
                    AuditAction.DAILY_LIMIT_RESET,
                    "Daily transaction limits reset",
                    "SYSTEM",
                    "SYSTEM",
                    null,
                    null,
                    null
            );

            log.info("Daily limit reset completed successfully");
        } catch (Exception e) {
            log.error("Failed to reset daily limits", e);
        }
    }

    /**
     * Hourly task to log system health metrics.
     * Runs at the start of every hour (HH:00:00).
     */
    @Scheduled(cron = "0 0 * * * *")
    public void logSystemHealth() {
        log.info("Starting hourly system health check");

        try {
            long activeUserCount = userRepository.count();

            auditService.log(
                    0L,
                    AuditAction.SYSTEM_HEALTH_CHECK,
                    "System health check - Active users: " + activeUserCount,
                    "SYSTEM",
                    "SYSTEM",
                    null,
                    null,
                    null
            );

            log.info("System health check completed - Active users: {}", activeUserCount);
        } catch (Exception e) {
            log.error("Failed to log system health", e);
        }
    }
}
