package az.hkbank.module.card.service.impl;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.common.util.CardNumberGenerator;
import az.hkbank.module.account.entity.Account;
import az.hkbank.module.account.entity.AccountStatus;
import az.hkbank.module.account.repository.AccountRepository;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.card.dto.*;
import az.hkbank.module.card.entity.Card;
import az.hkbank.module.card.entity.CardStatus;
import az.hkbank.module.card.mapper.CardMapper;
import az.hkbank.module.card.repository.CardRepository;
import az.hkbank.module.card.service.CardService;
import az.hkbank.module.notification.entity.NotificationType;
import az.hkbank.module.notification.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CardService interface.
 * Handles card management operations with audit logging.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private static final String ACTION_CARD_CREATED = "CARD_CREATED";
    private static final String ACTION_CARD_BLOCKED = "CARD_BLOCKED";
    private static final String ACTION_CARD_FROZEN = "CARD_FROZEN";
    private static final String ACTION_CARD_ACTIVATED = "CARD_ACTIVATED";
    private static final String ACTION_CARD_PIN_CHANGED = "CARD_PIN_CHANGED";
    private static final String ACTION_CARD_DELETED = "CARD_DELETED";
    private static final int MAX_CARDS_PER_ACCOUNT = 3;
    private static final int MAX_GENERATION_ATTEMPTS = 10;
    private static final int CARD_VALIDITY_YEARS = 3;

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final CardMapper cardMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    public CardResponse createCard(Long userId, CreateCardRequest request) {
        log.info("Creating card for user: {}, account: {}", userId, request.getAccountId());

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.getUser().getId().equals(userId)) {
            throw new BankException(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS);
        }

        if (account.isDeleted()) {
            throw new BankException(ErrorCode.ACCOUNT_NOT_FOUND, "Account is deleted");
        }

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BankException(ErrorCode.ACCOUNT_BLOCKED);
        }

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BankException(ErrorCode.ACCOUNT_CLOSED);
        }

        int activeCardsCount = cardRepository.countByAccountIdAndStatusNot(
                request.getAccountId(), CardStatus.BLOCKED);
        if (activeCardsCount >= MAX_CARDS_PER_ACCOUNT) {
            throw new BankException(ErrorCode.CARD_LIMIT_EXCEEDED,
                    "Maximum " + MAX_CARDS_PER_ACCOUNT + " cards allowed per account");
        }

        String cardNumber = generateUniqueCardNumber();
        String cvv = CardNumberGenerator.generateCvv();
        String pin = CardNumberGenerator.generatePin();
        String cardHolder = account.getUser().getFirstName() + " " + account.getUser().getLastName();
        LocalDate expiryDate = LocalDate.now().plusYears(CARD_VALIDITY_YEARS);

        Card card = Card.builder()
                .cardNumber(cardNumber)
                .cardHolder(cardHolder.toUpperCase())
                .expiryDate(expiryDate)
                .cvv(passwordEncoder.encode(cvv))
                .pin(passwordEncoder.encode(pin))
                .cardType(request.getCardType())
                .status(CardStatus.ACTIVE)
                .account(account)
                .isDeleted(false)
                .build();

        Card savedCard = cardRepository.save(card);

        auditService.log(
                userId,
                ACTION_CARD_CREATED,
                "Card created: " + CardNumberGenerator.maskCardNumber(cardNumber) + ", Type: " + request.getCardType(),
                getClientIpAddress()
        );

        log.info("Card created successfully: {}", CardNumberGenerator.maskCardNumber(cardNumber));

        return cardMapper.toCardResponse(savedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public CardResponse getCardById(Long cardId, Long userId) {
        log.info("Fetching card: {} for user: {}", cardId, userId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BankException(ErrorCode.CARD_NOT_FOUND));

        if (!card.getAccount().getUser().getId().equals(userId)) {
            throw new BankException(ErrorCode.UNAUTHORIZED_CARD_ACCESS);
        }

        if (card.isDeleted()) {
            throw new BankException(ErrorCode.CARD_NOT_FOUND, "Card is deleted");
        }

        return cardMapper.toCardResponse(card);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardSummaryResponse> getUserCards(Long userId) {
        log.info("Fetching all cards for user: {}", userId);

        List<Card> cards = cardRepository.findByAccountUserId(userId);

        return cards.stream()
                .filter(card -> !card.isDeleted())
                .map(cardMapper::toCardSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardSummaryResponse> getCardsByAccount(Long accountId, Long userId) {
        log.info("Fetching cards for account: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.getUser().getId().equals(userId)) {
            throw new BankException(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS);
        }

        List<Card> cards = cardRepository.findActiveCardsByAccountId(accountId);

        return cards.stream()
                .map(cardMapper::toCardSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CardResponse updateCardStatus(Long cardId, UpdateCardStatusRequest request, Long userId) {
        log.info("Updating card status: {} to {}", cardId, request.getStatus());

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BankException(ErrorCode.CARD_NOT_FOUND));

        if (!card.getAccount().getUser().getId().equals(userId)) {
            throw new BankException(ErrorCode.UNAUTHORIZED_CARD_ACCESS);
        }

        if (card.isDeleted()) {
            throw new BankException(ErrorCode.CARD_NOT_FOUND, "Card is deleted");
        }

        CardStatus oldStatus = card.getStatus();
        card.setStatus(request.getStatus());
        Card updatedCard = cardRepository.save(card);

        String action = getAuditActionForStatus(request.getStatus());
        String description = "Card status changed from " + oldStatus + " to " + request.getStatus();
        if (request.getReason() != null) {
            description += ". Reason: " + request.getReason();
        }

        auditService.log(
                userId,
                action,
                description + " - " + CardNumberGenerator.maskCardNumber(card.getCardNumber()),
                getClientIpAddress()
        );

        notificationService.createNotification(
                userId,
                NotificationType.CARD,
                "Kart statusu dəyişdirildi",
                "Kartınızın statusu dəyişdirildi: " + request.getStatus()
        );

        log.info("Card status updated successfully: {}", cardId);

        return cardMapper.toCardResponse(updatedCard);
    }

    @Override
    @Transactional
    public void changePin(Long cardId, ChangePinRequest request, Long userId) {
        log.info("Changing PIN for card: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BankException(ErrorCode.CARD_NOT_FOUND));

        if (!card.getAccount().getUser().getId().equals(userId)) {
            throw new BankException(ErrorCode.UNAUTHORIZED_CARD_ACCESS);
        }

        if (card.isDeleted()) {
            throw new BankException(ErrorCode.CARD_NOT_FOUND, "Card is deleted");
        }

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new BankException(ErrorCode.CARD_BLOCKED);
        }

        if (!passwordEncoder.matches(request.getCurrentPin(), card.getPin())) {
            throw new BankException(ErrorCode.INVALID_PIN);
        }

        card.setPin(passwordEncoder.encode(request.getNewPin()));
        cardRepository.save(card);

        auditService.log(
                userId,
                ACTION_CARD_PIN_CHANGED,
                "PIN changed for card: " + CardNumberGenerator.maskCardNumber(card.getCardNumber()),
                getClientIpAddress()
        );

        log.info("PIN changed successfully for card: {}", cardId);
    }

    @Override
    @Transactional
    public void softDeleteCard(Long cardId, Long userId) {
        log.info("Soft deleting card: {} for user: {}", cardId, userId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BankException(ErrorCode.CARD_NOT_FOUND));

        if (!card.getAccount().getUser().getId().equals(userId)) {
            throw new BankException(ErrorCode.UNAUTHORIZED_CARD_ACCESS);
        }

        if (card.isDeleted()) {
            throw new BankException(ErrorCode.CARD_NOT_FOUND, "Card is already deleted");
        }

        card.setDeleted(true);
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

        auditService.log(
                userId,
                ACTION_CARD_DELETED,
                "Card soft deleted: " + CardNumberGenerator.maskCardNumber(card.getCardNumber()),
                getClientIpAddress()
        );

        log.info("Card soft deleted successfully: {}", cardId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardSummaryResponse> getAllCards() {
        log.info("Fetching all active cards");

        List<Card> cards = cardRepository.findAllActive();

        return cards.stream()
                .map(cardMapper::toCardSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CardResponse adminUpdateCardStatus(Long cardId, UpdateCardStatusRequest request) {
        log.info("Admin updating card status: {} to {}", cardId, request.getStatus());

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BankException(ErrorCode.CARD_NOT_FOUND));

        if (card.isDeleted()) {
            throw new BankException(ErrorCode.CARD_NOT_FOUND, "Card is deleted");
        }

        CardStatus oldStatus = card.getStatus();
        card.setStatus(request.getStatus());
        Card updatedCard = cardRepository.save(card);

        String action = getAuditActionForStatus(request.getStatus());
        String description = "Admin: Card status changed from " + oldStatus + " to " + request.getStatus();
        if (request.getReason() != null) {
            description += ". Reason: " + request.getReason();
        }

        auditService.log(
                card.getAccount().getUser().getId(),
                action,
                description + " - " + CardNumberGenerator.maskCardNumber(card.getCardNumber()),
                getClientIpAddress()
        );

        log.info("Card status updated by admin successfully: {}", cardId);

        return cardMapper.toCardResponse(updatedCard);
    }

    private String generateUniqueCardNumber() {
        for (int i = 0; i < MAX_GENERATION_ATTEMPTS; i++) {
            String cardNumber = CardNumberGenerator.generateCardNumber();
            if (!cardRepository.existsByCardNumber(cardNumber)) {
                return cardNumber;
            }
        }
        throw new BankException(ErrorCode.INTERNAL_SERVER_ERROR,
                "Failed to generate unique card number");
    }

    private String getAuditActionForStatus(CardStatus status) {
        return switch (status) {
            case BLOCKED -> ACTION_CARD_BLOCKED;
            case FROZEN -> ACTION_CARD_FROZEN;
            case ACTIVE -> ACTION_CARD_ACTIVATED;
        };
    }

    private String getClientIpAddress() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return httpServletRequest.getRemoteAddr();
    }
}
