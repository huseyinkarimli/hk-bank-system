package az.hkbank.module.transaction;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.account.entity.Account;
import az.hkbank.module.account.entity.AccountStatus;
import az.hkbank.module.account.entity.CurrencyType;
import az.hkbank.module.account.mapper.AccountMapper;
import az.hkbank.module.account.repository.AccountRepository;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.card.entity.Card;
import az.hkbank.module.card.entity.CardStatus;
import az.hkbank.module.card.entity.CardType;
import az.hkbank.module.card.repository.CardRepository;
import az.hkbank.module.notification.service.NotificationService;
import az.hkbank.module.transaction.dto.P2PCardTransferRequest;
import az.hkbank.module.transaction.dto.P2PIbanTransferRequest;
import az.hkbank.module.transaction.dto.TransactionResponse;
import az.hkbank.module.transaction.entity.Transaction;
import az.hkbank.module.transaction.entity.TransactionStatus;
import az.hkbank.module.transaction.entity.TransactionType;
import az.hkbank.module.transaction.mapper.TransactionMapper;
import az.hkbank.module.transaction.repository.TransactionRepository;
import az.hkbank.module.transaction.service.ExchangeRateService;
import az.hkbank.module.transaction.service.FraudDetectionService;
import az.hkbank.module.transaction.service.TransactionLimitService;
import az.hkbank.module.transaction.service.impl.TransactionServiceImpl;
import az.hkbank.module.user.entity.Role;
import az.hkbank.module.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private FraudDetectionService fraudDetectionService;

    @Mock
    private TransactionLimitService transactionLimitService;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user1;
    private User user2;
    private Account senderAccount;
    private Account receiverAccount;
    private Card senderCard;
    private Card receiverCard;
    private Transaction transaction;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .firstName("Huseyin")
                .lastName("Karimli")
                .email("huseyin.karimli@hkbank.az")
                .password("$2a$10$encodedPassword")
                .phoneNumber("+994501234567")
                .role(Role.USER)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user2 = User.builder()
                .id(2L)
                .firstName("Anar")
                .lastName("Mammadov")
                .email("anar.mammadov@hkbank.az")
                .password("$2a$10$encodedPassword")
                .phoneNumber("+994501234568")
                .role(Role.USER)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        senderAccount = Account.builder()
                .id(1L)
                .accountNumber("1234567890")
                .iban("AZ21HKBA00000000001234567890")
                .balance(new BigDecimal("1000.00"))
                .currencyType(CurrencyType.AZN)
                .status(AccountStatus.ACTIVE)
                .user(user1)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        receiverAccount = Account.builder()
                .id(2L)
                .accountNumber("0987654321")
                .iban("AZ21HKBA00000000000987654321")
                .balance(new BigDecimal("500.00"))
                .currencyType(CurrencyType.AZN)
                .status(AccountStatus.ACTIVE)
                .user(user2)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        senderCard = Card.builder()
                .id(1L)
                .cardNumber("4422200712345678")
                .cardHolder("HUSEYIN KARIMLI")
                .expiryDate(LocalDate.now().plusYears(3))
                .cvv("$2a$10$encodedCvv")
                .pin("$2a$10$encodedPin")
                .cardType(CardType.DEBIT)
                .status(CardStatus.ACTIVE)
                .account(senderAccount)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        receiverCard = Card.builder()
                .id(2L)
                .cardNumber("4422200787654321")
                .cardHolder("ANAR MAMMADOV")
                .expiryDate(LocalDate.now().plusYears(3))
                .cvv("$2a$10$encodedCvv")
                .pin("$2a$10$encodedPin")
                .cardType(CardType.DEBIT)
                .status(CardStatus.ACTIVE)
                .account(receiverAccount)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .referenceNumber("TXN12345678")
                .type(TransactionType.P2P_CARD)
                .status(TransactionStatus.SUCCESS)
                .amount(new BigDecimal("100.00"))
                .convertedAmount(new BigDecimal("100.00"))
                .sourceCurrency(CurrencyType.AZN)
                .targetCurrency(CurrencyType.AZN)
                .senderAccount(senderAccount)
                .receiverAccount(receiverAccount)
                .senderCard(senderCard)
                .receiverCard(receiverCard)
                .ipAddress("127.0.0.1")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        transactionResponse = TransactionResponse.builder()
                .id(1L)
                .referenceNumber("TXN12345678")
                .type(TransactionType.P2P_CARD)
                .status(TransactionStatus.SUCCESS)
                .amount(new BigDecimal("100.00"))
                .convertedAmount(new BigDecimal("100.00"))
                .sourceCurrency(CurrencyType.AZN)
                .targetCurrency(CurrencyType.AZN)
                .senderAccountNumber("1234567890")
                .receiverAccountNumber("0987654321")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void transferByCard_Success_SameCurrency() {
        P2PCardTransferRequest request = P2PCardTransferRequest.builder()
                .sourceCardNumber("4422200712345678")
                .targetCardNumber("4422200787654321")
                .amount(new BigDecimal("100.00"))
                .description("Test transfer")
                .build();

        when(cardRepository.findByCardNumber("4422200712345678")).thenReturn(Optional.of(senderCard));
        when(cardRepository.findByCardNumber("4422200787654321")).thenReturn(Optional.of(receiverCard));
        doNothing().when(fraudDetectionService).checkForFraud(anyLong(), anyLong(), anyString());
        doNothing().when(transactionLimitService).checkTransactionLimit(anyLong(), any(BigDecimal.class), any(CurrencyType.class));
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(receiverAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toTransactionResponse(any(Transaction.class))).thenReturn(transactionResponse);

        TransactionResponse response = transactionService.transferByCard(1L, request, "127.0.0.1");

        assertNotNull(response);
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(new BigDecimal("100.00"), response.getAmount());

        verify(cardRepository).findByCardNumber("4422200712345678");
        verify(cardRepository).findByCardNumber("4422200787654321");
        verify(fraudDetectionService).checkForFraud(1L, 1L, "127.0.0.1");
        verify(transactionLimitService).checkTransactionLimit(eq(1L), any(BigDecimal.class), eq(CurrencyType.AZN));
        verify(accountRepository).findByIdForUpdate(1L);
        verify(accountRepository).findByIdForUpdate(2L);
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
        verify(auditService).log(eq(1L), anyString(), anyString(), anyString());
    }

    @Test
    void transferByCard_Success_DifferentCurrency() {
        receiverAccount.setCurrencyType(CurrencyType.USD);

        P2PCardTransferRequest request = P2PCardTransferRequest.builder()
                .sourceCardNumber("4422200712345678")
                .targetCardNumber("4422200787654321")
                .amount(new BigDecimal("100.00"))
                .build();

        when(cardRepository.findByCardNumber("4422200712345678")).thenReturn(Optional.of(senderCard));
        when(cardRepository.findByCardNumber("4422200787654321")).thenReturn(Optional.of(receiverCard));
        doNothing().when(fraudDetectionService).checkForFraud(anyLong(), anyLong(), anyString());
        doNothing().when(transactionLimitService).checkTransactionLimit(anyLong(), any(BigDecimal.class), any(CurrencyType.class));
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(receiverAccount));
        when(exchangeRateService.getExchangeRate(CurrencyType.AZN, CurrencyType.USD))
                .thenReturn(new BigDecimal("0.588235"));
        when(exchangeRateService.convert(new BigDecimal("100.00"), CurrencyType.AZN, CurrencyType.USD))
                .thenReturn(new BigDecimal("58.82"));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toTransactionResponse(any(Transaction.class))).thenReturn(transactionResponse);
        doNothing().when(notificationService).createNotification(anyLong(), any(), anyString(), anyString());

        TransactionResponse response = transactionService.transferByCard(1L, request, "127.0.0.1");

        assertNotNull(response);
        verify(exchangeRateService).getExchangeRate(CurrencyType.AZN, CurrencyType.USD);
        verify(exchangeRateService).convert(new BigDecimal("100.00"), CurrencyType.AZN, CurrencyType.USD);
    }

    @Test
    void transferByCard_InsufficientBalance_ThrowsBankException() {
        senderAccount.setBalance(new BigDecimal("50.00"));

        P2PCardTransferRequest request = P2PCardTransferRequest.builder()
                .sourceCardNumber("4422200712345678")
                .targetCardNumber("4422200787654321")
                .amount(new BigDecimal("100.00"))
                .build();

        when(cardRepository.findByCardNumber("4422200712345678")).thenReturn(Optional.of(senderCard));
        when(cardRepository.findByCardNumber("4422200787654321")).thenReturn(Optional.of(receiverCard));
        doNothing().when(fraudDetectionService).checkForFraud(anyLong(), anyLong(), anyString());
        doNothing().when(transactionLimitService).checkTransactionLimit(anyLong(), any(BigDecimal.class), any(CurrencyType.class));
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(receiverAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BankException exception = assertThrows(BankException.class, () -> {
            transactionService.transferByCard(1L, request, "127.0.0.1");
        });

        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, exception.getErrorCode());
        verify(transactionRepository, atLeastOnce()).save(argThat(t -> t.getStatus() == TransactionStatus.REJECTED));
        verify(auditService).log(eq(1L), anyString(), contains("rejected"), anyString());
    }

    @Test
    void transferByCard_CardBlocked_ThrowsBankException() {
        senderCard.setStatus(CardStatus.BLOCKED);

        P2PCardTransferRequest request = P2PCardTransferRequest.builder()
                .sourceCardNumber("4422200712345678")
                .targetCardNumber("4422200787654321")
                .amount(new BigDecimal("100.00"))
                .build();

        when(cardRepository.findByCardNumber("4422200712345678")).thenReturn(Optional.of(senderCard));

        BankException exception = assertThrows(BankException.class, () -> {
            transactionService.transferByCard(1L, request, "127.0.0.1");
        });

        assertEquals(ErrorCode.CARD_BLOCKED, exception.getErrorCode());
        verify(cardRepository).findByCardNumber("4422200712345678");
        verify(fraudDetectionService, never()).checkForFraud(anyLong(), anyLong(), anyString());
    }

    @Test
    void transferByCard_CardFrozen_ThrowsBankException() {
        senderCard.setStatus(CardStatus.FROZEN);

        P2PCardTransferRequest request = P2PCardTransferRequest.builder()
                .sourceCardNumber("4422200712345678")
                .targetCardNumber("4422200787654321")
                .amount(new BigDecimal("100.00"))
                .build();

        when(cardRepository.findByCardNumber("4422200712345678")).thenReturn(Optional.of(senderCard));

        BankException exception = assertThrows(BankException.class, () -> {
            transactionService.transferByCard(1L, request, "127.0.0.1");
        });

        assertEquals(ErrorCode.CARD_FROZEN, exception.getErrorCode());
        verify(cardRepository).findByCardNumber("4422200712345678");
    }

    @Test
    void transferByCard_FraudDetected_ThrowsBankException() {
        P2PCardTransferRequest request = P2PCardTransferRequest.builder()
                .sourceCardNumber("4422200712345678")
                .targetCardNumber("4422200787654321")
                .amount(new BigDecimal("100.00"))
                .build();

        when(cardRepository.findByCardNumber("4422200712345678")).thenReturn(Optional.of(senderCard));
        when(cardRepository.findByCardNumber("4422200787654321")).thenReturn(Optional.of(receiverCard));
        doThrow(new BankException(ErrorCode.FRAUD_DETECTED))
                .when(fraudDetectionService).checkForFraud(anyLong(), anyLong(), anyString());

        BankException exception = assertThrows(BankException.class, () -> {
            transactionService.transferByCard(1L, request, "127.0.0.1");
        });

        assertEquals(ErrorCode.FRAUD_DETECTED, exception.getErrorCode());
        verify(fraudDetectionService).checkForFraud(1L, 1L, "127.0.0.1");
    }

    @Test
    void transferByCard_DailyLimitExceeded_ThrowsBankException() {
        P2PCardTransferRequest request = P2PCardTransferRequest.builder()
                .sourceCardNumber("4422200712345678")
                .targetCardNumber("4422200787654321")
                .amount(new BigDecimal("100.00"))
                .build();

        when(cardRepository.findByCardNumber("4422200712345678")).thenReturn(Optional.of(senderCard));
        when(cardRepository.findByCardNumber("4422200787654321")).thenReturn(Optional.of(receiverCard));
        doNothing().when(fraudDetectionService).checkForFraud(anyLong(), anyLong(), anyString());
        doThrow(new BankException(ErrorCode.DAILY_LIMIT_EXCEEDED))
                .when(transactionLimitService).checkTransactionLimit(anyLong(), any(BigDecimal.class), any(CurrencyType.class));

        BankException exception = assertThrows(BankException.class, () -> {
            transactionService.transferByCard(1L, request, "127.0.0.1");
        });

        assertEquals(ErrorCode.DAILY_LIMIT_EXCEEDED, exception.getErrorCode());
        verify(transactionLimitService).checkTransactionLimit(eq(1L), any(BigDecimal.class), eq(CurrencyType.AZN));
    }

    @Test
    void transferByCard_Rollback_OnException() {
        P2PCardTransferRequest request = P2PCardTransferRequest.builder()
                .sourceCardNumber("4422200712345678")
                .targetCardNumber("4422200787654321")
                .amount(new BigDecimal("100.00"))
                .build();

        when(cardRepository.findByCardNumber("4422200712345678")).thenReturn(Optional.of(senderCard));
        when(cardRepository.findByCardNumber("4422200787654321")).thenReturn(Optional.of(receiverCard));
        doNothing().when(fraudDetectionService).checkForFraud(anyLong(), anyLong(), anyString());
        doNothing().when(transactionLimitService).checkTransactionLimit(anyLong(), any(BigDecimal.class), any(CurrencyType.class));
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(receiverAccount));
        when(accountRepository.save(any(Account.class))).thenThrow(new RuntimeException("Database error"));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BankException exception = assertThrows(BankException.class, () -> {
            transactionService.transferByCard(1L, request, "127.0.0.1");
        });

        assertEquals(ErrorCode.TRANSACTION_FAILED, exception.getErrorCode());
        verify(transactionRepository, atLeastOnce()).save(argThat(t -> t.getStatus() == TransactionStatus.FAILED));
        verify(auditService).log(eq(1L), anyString(), contains("failed"), anyString());
    }

    @Test
    void transferByIban_Success() {
        P2PIbanTransferRequest request = P2PIbanTransferRequest.builder()
                .sourceIban("AZ21HKBA00000000001234567890")
                .targetIban("AZ21HKBA00000000000987654321")
                .amount(new BigDecimal("100.00"))
                .description("IBAN transfer")
                .build();

        when(accountRepository.findByIban("AZ21HKBA00000000001234567890")).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByIban("AZ21HKBA00000000000987654321")).thenReturn(Optional.of(receiverAccount));
        doNothing().when(transactionLimitService).checkTransactionLimit(anyLong(), any(BigDecimal.class), any(CurrencyType.class));
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(receiverAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toTransactionResponse(any(Transaction.class))).thenReturn(transactionResponse);
        doNothing().when(notificationService).createNotification(anyLong(), any(), anyString(), anyString());

        TransactionResponse response = transactionService.transferByIban(1L, request, "127.0.0.1");

        assertNotNull(response);
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());

        verify(accountRepository).findByIban("AZ21HKBA00000000001234567890");
        verify(accountRepository).findByIban("AZ21HKBA00000000000987654321");
        verify(accountRepository).findByIdForUpdate(1L);
        verify(accountRepository).findByIdForUpdate(2L);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transferByIban_InvalidIban_ThrowsBankException() {
        P2PIbanTransferRequest request = P2PIbanTransferRequest.builder()
                .sourceIban("AZ21HKBA00000000001234567890")
                .targetIban("INVALID_IBAN")
                .amount(new BigDecimal("100.00"))
                .build();

        when(accountRepository.findByIban("AZ21HKBA00000000001234567890")).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByIban("INVALID_IBAN")).thenReturn(Optional.empty());

        BankException exception = assertThrows(BankException.class, () -> {
            transactionService.transferByIban(1L, request, "127.0.0.1");
        });

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
        verify(accountRepository).findByIban("INVALID_IBAN");
    }

    @Test
    void getTransactionById_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toTransactionResponse(transaction)).thenReturn(transactionResponse);

        TransactionResponse response = transactionService.getTransactionById(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("TXN12345678", response.getReferenceNumber());

        verify(transactionRepository).findById(1L);
    }
}
