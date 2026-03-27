package az.hkbank.module.user.service;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.user.entity.RefreshToken;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int REFRESH_TOKEN_VALIDITY_DAYS = 30;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public String generateRefreshToken(User user) {
        refreshTokenRepository.revokeAllByUserId(user.getId());

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS);
        RefreshToken entity = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        return refreshTokenRepository.save(entity).getToken();
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BankException(ErrorCode.INVALID_TOKEN, "Refresh token etibarsızdır"));

        if (refreshToken.isRevoked()) {
            throw new BankException(ErrorCode.INVALID_TOKEN, "Refresh token ləğv edilmişdir");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BankException(ErrorCode.TOKEN_EXPIRED, "Refresh token müddəti bitmişdir");
        }

        return refreshToken;
    }
}
