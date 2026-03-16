package az.hkbank.module.card;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.account.entity.Account;
import az.hkbank.module.account.entity.AccountStatus;
import az.hkbank.module.account.entity.CurrencyType;
import az.hkbank.module.account.repository.AccountRepository;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.card.dto.*;
import az.hkbank.module.card.entity.Card;
import az.hkbank.module.card.entity.CardStatus;
import az.hkbank.module.card.entity.CardType;
import az.hkbank.module.card.mapper.CardMapper;
import az.hkbank.module.card.repository.CardRepository;
import az.hkbank.module.card.service.impl.CardServiceImpl;
import az.hkbank.module.user.entity.Role;
import az.hkbank.module.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CardServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CardServiceImpl cardService;

    private User user;
    private Account account;
    private Card card;
    private CreateCardRequest createCardRequest;
    private CardResponse cardResponse;
    private CardSummaryResponse cardSummaryResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@hkbank.az")
                .password("$2a$10$encodedPassword")
                .phoneNumber("+994501234567")
                .role(Role.USER)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        account = Account.builder()
                .id(1L)
                .accountNumber("1234567890")
                .iban("AZ21HKBA00000000001234567890")
                .balance(new BigDecimal("100.00"))
                .currencyType(CurrencyType.AZN)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        card = Card.builder()
                .id(1L)
                .cardNumber("4422200712345678")
                .cardHolder("JOHN DOE")
                .expiryDate(LocalDate.now().plusYears(3))
                .cvv("$2a$10$encodedCvv")
                .pin("$2a$10$encodedPin")
                .cardType(CardType.DEBIT)
                .status(CardStatus.ACTIVE)
                .account(account)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createCardRequest = CreateCardRequest.builder()
                .accountId(1L)
                .cardType(CardType.DEBIT)
                .build();

        cardResponse = CardResponse.builder()
                .id(1L)
                .maskedCardNumber("4422 **** **** 5678")
                .cardHolder("JOHN DOE")
                .expiryDate(LocalDate.now().plusYears(3))
                .cardType(CardType.DEBIT)
                .status(CardStatus.ACTIVE)
                .accountId(1L)
                .createdAt(LocalDateTime.now())
                .build();

        cardSummaryResponse = CardSummaryResponse.builder()
                .id(1L)
                .maskedCardNumber("4422 **** **** 5678")
                .cardHolder("JOHN DOE")
                .cardType(CardType.DEBIT)
                .status(CardStatus.ACTIVE)
                .expiryDate(LocalDate.now().plusYears(3))
                .build();
    }

    @Test
    void createCard_Success_Debit() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(cardRepository.countByAccountIdAndStatusNot(1L, CardStatus.BLOCKED)).thenReturn(0);
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toCardResponse(any(Card.class))).thenReturn(cardResponse);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        CardResponse response = cardService.createCard(1L, createCardRequest);

        assertNotNull(response);
        assertEquals("4422 **** **** 5678", response.getMaskedCardNumber());
        assertEquals(CardType.DEBIT, response.getCardType());
        assertEquals(CardStatus.ACTIVE, response.getStatus());

        verify(accountRepository).findById(1L);
        verify(cardRepository).countByAccountIdAndStatusNot(1L, CardStatus.BLOCKED);
        verify(cardRepository).save(any(Card.class));
        verify(auditService).log(eq(1L), anyString(), anyString(), anyString());
    }

    @Test
    void createCard_CardLimitExceeded_ThrowsBankException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(cardRepository.countByAccountIdAndStatusNot(1L, CardStatus.BLOCKED)).thenReturn(3);

        BankException exception = assertThrows(BankException.class, () -> {
            cardService.createCard(1L, createCardRequest);
        });

        assertEquals(ErrorCode.CARD_LIMIT_EXCEEDED, exception.getErrorCode());

        verify(accountRepository).findById(1L);
        verify(cardRepository).countByAccountIdAndStatusNot(1L, CardStatus.BLOCKED);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void createCard_AccountNotFound_ThrowsBankException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        BankException exception = assertThrows(BankException.class, () -> {
            cardService.createCard(1L, createCardRequest);
        });

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());

        verify(accountRepository).findById(1L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void getCardById_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardMapper.toCardResponse(card)).thenReturn(cardResponse);

        CardResponse response = cardService.getCardById(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("4422 **** **** 5678", response.getMaskedCardNumber());

        verify(cardRepository).findById(1L);
    }

    @Test
    void getCardById_NotOwner_ThrowsBankException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        BankException exception = assertThrows(BankException.class, () -> {
            cardService.getCardById(1L, 999L);
        });

        assertEquals(ErrorCode.UNAUTHORIZED_CARD_ACCESS, exception.getErrorCode());

        verify(cardRepository).findById(1L);
    }

    @Test
    void getUserCards_Success() {
        List<Card> cards = List.of(card);
        when(cardRepository.findByAccountUserId(1L)).thenReturn(cards);
        when(cardMapper.toCardSummaryResponse(card)).thenReturn(cardSummaryResponse);

        List<CardSummaryResponse> response = cardService.getUserCards(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("4422 **** **** 5678", response.get(0).getMaskedCardNumber());

        verify(cardRepository).findByAccountUserId(1L);
    }

    @Test
    void changePin_Success() {
        ChangePinRequest pinRequest = ChangePinRequest.builder()
                .currentPin("1234")
                .newPin("5678")
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(passwordEncoder.matches("1234", card.getPin())).thenReturn(true);
        when(passwordEncoder.encode("5678")).thenReturn("$2a$10$newEncodedPin");
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        cardService.changePin(1L, pinRequest, 1L);

        verify(cardRepository).findById(1L);
        verify(passwordEncoder).matches(eq("1234"), anyString());
        verify(passwordEncoder).encode("5678");
        verify(cardRepository).save(any(Card.class));
        verify(auditService).log(eq(1L), anyString(), anyString(), anyString());
    }

    @Test
    void changePin_InvalidPin_ThrowsBankException() {
        ChangePinRequest pinRequest = ChangePinRequest.builder()
                .currentPin("1234")
                .newPin("5678")
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(passwordEncoder.matches("1234", card.getPin())).thenReturn(false);

        BankException exception = assertThrows(BankException.class, () -> {
            cardService.changePin(1L, pinRequest, 1L);
        });

        assertEquals(ErrorCode.INVALID_PIN, exception.getErrorCode());

        verify(cardRepository).findById(1L);
        verify(passwordEncoder).matches("1234", card.getPin());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void softDeleteCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        cardService.softDeleteCard(1L, 1L);

        verify(cardRepository).findById(1L);
        verify(cardRepository).save(argThat(c -> c.isDeleted() && c.getStatus() == CardStatus.BLOCKED));
        verify(auditService).log(eq(1L), anyString(), anyString(), anyString());
    }
}
