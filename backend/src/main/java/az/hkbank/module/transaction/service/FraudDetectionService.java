package az.hkbank.module.transaction.service;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.card.entity.Card;
import az.hkbank.module.card.entity.CardStatus;
import az.hkbank.module.card.repository.CardRepository;
import az.hkbank.module.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for detecting and preventing fraudulent transactions.
 * Monitors transaction patterns and automatically freezes suspicious cards.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private static final int FRAUD_THRESHOLD_COUNT = 3;
    private static final int FRAUD_DETECTION_WINDOW_SECONDS = 60;
    private static final String ACTION_FRAUD_DETECTED = "FRAUD_DETECTED";

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final AuditService auditService;

    /**
     * Checks for fraudulent activity on a card.
     * If 3+ transactions detected within 60 seconds, freezes the card.
     *
     * @param cardId the card ID to check
     * @param userId the user ID for audit logging
     * @param ipAddress the IP address for audit logging
     * @throws BankException if fraud is detected
     */
    @Transactional
    public void checkForFraud(Long cardId, Long userId, String ipAddress) {
        LocalDateTime windowStart = LocalDateTime.now().minusSeconds(FRAUD_DETECTION_WINDOW_SECONDS);

        int recentTransactionCount = transactionRepository
                .countBySenderCardIdAndCreatedAtAfter(cardId, windowStart);

        log.debug("Fraud check for card {}: {} transactions in last {} seconds",
                cardId, recentTransactionCount, FRAUD_DETECTION_WINDOW_SECONDS);

        if (recentTransactionCount >= FRAUD_THRESHOLD_COUNT) {
            log.warn("FRAUD DETECTED: Card {} has {} transactions in {} seconds",
                    cardId, recentTransactionCount, FRAUD_DETECTION_WINDOW_SECONDS);

            Card card = cardRepository.findById(cardId)
                    .orElseThrow(() -> new BankException(ErrorCode.CARD_NOT_FOUND));

            card.setStatus(CardStatus.FROZEN);
            cardRepository.save(card);

            auditService.log(
                    userId,
                    ACTION_FRAUD_DETECTED,
                    "Card frozen due to suspicious activity: " + recentTransactionCount +
                            " transactions in " + FRAUD_DETECTION_WINDOW_SECONDS + " seconds",
                    ipAddress
            );

            throw new BankException(ErrorCode.FRAUD_DETECTED,
                    "Card has been frozen due to suspicious activity");
        }
    }
}
