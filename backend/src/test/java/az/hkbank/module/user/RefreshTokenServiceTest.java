package az.hkbank.module.user;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.user.entity.RefreshToken;
import az.hkbank.module.user.entity.Role;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.user.repository.RefreshTokenRepository;
import az.hkbank.module.user.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("A")
                .lastName("B")
                .email("a@hkbank.az")
                .password("p")
                .phoneNumber("+994000000001")
                .role(Role.USER)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void generateRefreshToken_Success() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken rt = invocation.getArgument(0);
            rt.setId(10L);
            return rt;
        });

        String token = refreshTokenService.generateRefreshToken(user);

        assertNotNull(token);
        verify(refreshTokenRepository).revokeAllByUserId(1L);
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();
        assertEquals(token, saved.getToken());
        assertEquals(user, saved.getUser());
        assertFalse(saved.isRevoked());
        assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now().plusDays(29)));
    }

    @Test
    void generateRefreshToken_RevokesExisting() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        refreshTokenService.generateRefreshToken(user);

        verify(refreshTokenRepository, times(1)).revokeAllByUserId(1L);
    }

    @Test
    void validateRefreshToken_Success() {
        RefreshToken rt = RefreshToken.builder()
                .id(1L)
                .token("tok")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("tok")).thenReturn(Optional.of(rt));

        RefreshToken result = refreshTokenService.validateRefreshToken("tok");

        assertSame(rt, result);
    }

    @Test
    void validateRefreshToken_Revoked_ThrowsBankException() {
        RefreshToken rt = RefreshToken.builder()
                .token("tok")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(true)
                .build();
        when(refreshTokenRepository.findByToken("tok")).thenReturn(Optional.of(rt));

        BankException ex = assertThrows(BankException.class, () -> refreshTokenService.validateRefreshToken("tok"));
        assertEquals(ErrorCode.INVALID_TOKEN, ex.getErrorCode());
    }

    @Test
    void validateRefreshToken_Expired_ThrowsBankException() {
        RefreshToken rt = RefreshToken.builder()
                .token("tok")
                .user(user)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("tok")).thenReturn(Optional.of(rt));

        BankException ex = assertThrows(BankException.class, () -> refreshTokenService.validateRefreshToken("tok"));
        assertEquals(ErrorCode.TOKEN_EXPIRED, ex.getErrorCode());
    }

    @Test
    void validateRefreshToken_NotFound_ThrowsBankException() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        BankException ex = assertThrows(BankException.class, () -> refreshTokenService.validateRefreshToken("missing"));
        assertEquals(ErrorCode.INVALID_TOKEN, ex.getErrorCode());
    }
}
