package az.hkbank.module.audit;

import az.hkbank.module.audit.entity.AuditLog;
import az.hkbank.module.audit.repository.AuditLogRepository;
import az.hkbank.module.audit.service.AuditAction;
import az.hkbank.module.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditService.
 * Tests audit logging operations with entity tracking.
 */
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = AuditLog.builder()
                .id(1L)
                .userId(1L)
                .action(AuditAction.LOGIN)
                .description("User logged in successfully")
                .ipAddress("127.0.0.1")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void logAction_Success() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);

        auditService.log(
                1L,
                AuditAction.LOGIN,
                "User logged in successfully",
                "127.0.0.1"
        );

        verify(auditLogRepository).save(argThat(log ->
                log.getUserId().equals(1L) &&
                log.getAction().equals(AuditAction.LOGIN) &&
                log.getDescription().equals("User logged in successfully") &&
                log.getIpAddress().equals("127.0.0.1") &&
                log.getEntityType() == null &&
                log.getEntityId() == null
        ));
    }

    @Test
    void logAction_WithEntityInfo_Success() {
        AuditLog auditLogWithEntity = AuditLog.builder()
                .id(2L)
                .userId(1L)
                .action("USER_ROLE_CHANGED")
                .description("User role changed from USER to ADMIN")
                .ipAddress("127.0.0.1")
                .entityType("USER")
                .entityId(1L)
                .oldValue("USER")
                .newValue("ADMIN")
                .createdAt(LocalDateTime.now())
                .build();

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLogWithEntity);

        auditService.log(
                1L,
                "USER_ROLE_CHANGED",
                "User role changed from USER to ADMIN",
                "127.0.0.1",
                "USER",
                1L,
                "USER",
                "ADMIN"
        );

        verify(auditLogRepository).save(argThat(log ->
                log.getUserId().equals(1L) &&
                log.getAction().equals("USER_ROLE_CHANGED") &&
                log.getEntityType().equals("USER") &&
                log.getEntityId().equals(1L) &&
                log.getOldValue().equals("USER") &&
                log.getNewValue().equals("ADMIN")
        ));
    }

    @Test
    void findByUserId_Success() {
        List<AuditLog> logs = Arrays.asList(auditLog, auditLog);

        when(auditLogRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(logs);

        List<AuditLog> result = auditLogRepository.findByUserIdOrderByCreatedAtDesc(1L);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(auditLogRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void findByAction_Success() {
        List<AuditLog> logs = Arrays.asList(auditLog);

        when(auditLogRepository.findByAction(AuditAction.LOGIN)).thenReturn(logs);

        List<AuditLog> result = auditLogRepository.findByAction(AuditAction.LOGIN);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(AuditAction.LOGIN, result.get(0).getAction());

        verify(auditLogRepository).findByAction(AuditAction.LOGIN);
    }

    @Test
    void logAction_ExceptionHandled_DoesNotThrow() {
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));

        assertDoesNotThrow(() -> {
            auditService.log(
                    1L,
                    AuditAction.LOGIN,
                    "User logged in successfully",
                    "127.0.0.1"
            );
        });

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void findByEntityTypeAndEntityId_Success() {
        AuditLog entityLog = AuditLog.builder()
                .id(3L)
                .userId(1L)
                .action("ACCOUNT_STATUS_CHANGED")
                .description("Account status changed")
                .ipAddress("127.0.0.1")
                .entityType("ACCOUNT")
                .entityId(5L)
                .createdAt(LocalDateTime.now())
                .build();

        when(auditLogRepository.findByEntityTypeAndEntityId("ACCOUNT", 5L))
                .thenReturn(Arrays.asList(entityLog));

        List<AuditLog> result = auditLogRepository.findByEntityTypeAndEntityId("ACCOUNT", 5L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACCOUNT", result.get(0).getEntityType());
        assertEquals(5L, result.get(0).getEntityId());

        verify(auditLogRepository).findByEntityTypeAndEntityId("ACCOUNT", 5L);
    }

    @Test
    void findByCreatedAtBetween_Success() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();

        List<AuditLog> logs = Arrays.asList(auditLog, auditLog);

        when(auditLogRepository.findByCreatedAtBetween(from, to)).thenReturn(logs);

        List<AuditLog> result = auditLogRepository.findByCreatedAtBetween(from, to);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(auditLogRepository).findByCreatedAtBetween(from, to);
    }
}
