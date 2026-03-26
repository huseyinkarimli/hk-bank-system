package az.hkbank.module.admin;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.config.jwt.JwtService;
import az.hkbank.module.admin.service.AdminUserBanService;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.card.entity.Card;
import az.hkbank.module.card.entity.CardStatus;
import az.hkbank.module.card.repository.CardRepository;
import az.hkbank.module.notification.entity.NotificationType;
import az.hkbank.module.notification.service.NotificationService;
import az.hkbank.module.user.entity.Role;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AdminUserBanService}.
 */
@ExtendWith(MockitoExtension.class)
class AdminUserBanServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AdminUserBanService adminUserBanService;

    private User user;
    private Card card1;
    private Card card2;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(7L)
                .firstName("Test")
                .lastName("User")
                .email("test@hkbank.az")
                .password("x")
                .phoneNumber("+994000000001")
                .role(Role.USER)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        card1 = Card.builder().id(1L).status(CardStatus.ACTIVE).build();
        card2 = Card.builder().id(2L).status(CardStatus.FROZEN).build();
    }

    @Test
    void banUser_softDeletesBlocksCardsJwtAuditNotification() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(cardRepository.findByAccountUserId(7L)).thenReturn(List.of(card1, card2));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        adminUserBanService.banUser(7L, "Suspicious activity", "192.168.1.1");

        assertTrue(user.isDeleted());
        verify(userRepository).save(user);

        verify(cardRepository).saveAll(argThat((List<Card> list) ->
                list.size() == 2 && list.stream().allMatch(c -> c.getStatus() == CardStatus.BLOCKED)));

        verify(jwtService).banUser(7L);
        verify(auditService).log(7L, "USER_BANNED", "Suspicious activity", "192.168.1.1");
        verify(notificationService).createNotification(
                eq(7L),
                eq(NotificationType.SECURITY),
                eq("Hesab bloklaması"),
                eq("Hesabınız bloklanmışdır: Suspicious activity")
        );
    }

    @Test
    void banUser_userNotFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        BankException ex = assertThrows(BankException.class,
                () -> adminUserBanService.banUser(99L, "reason", "127.0.0.1"));
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        verify(jwtService, never()).banUser(anyLong());
    }
}
