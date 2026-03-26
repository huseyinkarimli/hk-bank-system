package az.hkbank.module.admin.service;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.config.jwt.JwtService;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.card.entity.Card;
import az.hkbank.module.card.entity.CardStatus;
import az.hkbank.module.card.repository.CardRepository;
import az.hkbank.module.notification.entity.NotificationType;
import az.hkbank.module.notification.service.NotificationService;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Admin flow: ban user (soft delete), block all cards, JWT deny-list, audit, notification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserBanService {

    private static final String ACTION_USER_BANNED = "USER_BANNED";

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Transactional(rollbackFor = Exception.class)
    public void banUser(Long userId, String reason, String ipAddress) {
        log.info("Admin banning user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BankException(ErrorCode.USER_NOT_FOUND));

        user.setDeleted(true);
        userRepository.save(user);

        List<Card> cards = cardRepository.findByAccountUserId(userId);
        for (Card card : cards) {
            card.setStatus(CardStatus.BLOCKED);
        }
        cardRepository.saveAll(cards);

        jwtService.banUser(userId);

        auditService.log(userId, ACTION_USER_BANNED, reason, ipAddress);

        notificationService.createNotification(
                userId,
                NotificationType.SECURITY,
                "Hesab bloklaması",
                "Hesabınız bloklanmışdır: " + reason
        );

        log.info("User {} banned; {} cards blocked", userId, cards.size());
    }
}
