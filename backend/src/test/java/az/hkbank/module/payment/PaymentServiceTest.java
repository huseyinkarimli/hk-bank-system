package az.hkbank.module.payment;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.account.entity.Account;
import az.hkbank.module.account.entity.AccountStatus;
import az.hkbank.module.account.entity.CurrencyType;
import az.hkbank.module.account.repository.AccountRepository;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.payment.dto.PaymentRequest;
import az.hkbank.module.payment.dto.PaymentResponse;
import az.hkbank.module.payment.dto.PaymentSummaryResponse;
import az.hkbank.module.payment.entity.Payment;
import az.hkbank.module.payment.entity.PaymentStatus;
import az.hkbank.module.payment.entity.ProviderType;
import az.hkbank.module.payment.mapper.PaymentMapper;
import az.hkbank.module.payment.repository.PaymentRepository;
import az.hkbank.module.payment.service.ProviderSimulationService;
import az.hkbank.module.payment.service.impl.PaymentServiceImpl;
import az.hkbank.module.user.entity.Role;
import az.hkbank.module.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentServiceImpl.
 * Tests payment processing, validation, provider simulation, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private ProviderSimulationService providerSimulationService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User user;
    private Account account;
    private Payment payment;
    private PaymentResponse paymentResponse;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
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

        account = Account.builder()
                .id(1L)
                .accountNumber("1234567890")
                .iban("AZ21HKBA00000000001234567890")
                .balance(new BigDecimal("1000.00"))
                .currencyType(CurrencyType.AZN)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        paymentRequest = PaymentRequest.builder()
                .accountId(1L)
                .providerType(ProviderType.MOBILE)
                .providerName("Azercell")
                .subscriberNumber("+994501234567")
                .amount(new BigDecimal("50.00"))
                .description("Mobile payment")
                .build();

        payment = Payment.builder()
                .id(1L)
                .referenceNumber("PAY12345678")
                .providerType(ProviderType.MOBILE)
                .providerName("Azercell")
                .subscriberNumber("+994501234567")
                .amount(new BigDecimal("50.00"))
                .status(PaymentStatus.SUCCESS)
                .account(account)
                .description("Mobile payment")
                .ipAddress("127.0.0.1")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        paymentResponse = PaymentResponse.builder()
                .id(1L)
                .referenceNumber("PAY12345678")
                .providerType(ProviderType.MOBILE)
                .providerName("Azercell")
                .subscriberNumber("+994501234567")
                .amount(new BigDecimal("50.00"))
                .status(PaymentStatus.SUCCESS)
                .accountNumber("******7890")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void makePayment_Success_Mobile() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(providerSimulationService.simulatePayment(
                eq(ProviderType.MOBILE), eq("+994501234567"), any(BigDecimal.class)))
                .thenReturn(true);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(paymentResponse);
        doNothing().when(auditService).log(anyLong(), anyString(), anyString(), anyString());

        PaymentResponse response = paymentService.makePayment(1L, paymentRequest, "127.0.0.1");

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals(new BigDecimal("50.00"), response.getAmount());
        assertEquals("Azercell", response.getProviderName());

        verify(accountRepository).findById(1L);
        verify(accountRepository).findByIdForUpdate(1L);
        verify(providerSimulationService).simulatePayment(eq(ProviderType.MOBILE), eq("+994501234567"), any(BigDecimal.class));
        verify(accountRepository, atLeastOnce()).save(any(Account.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(auditService).log(eq(1L), anyString(), contains("completed"), anyString());
    }

    @Test
    void makePayment_InsufficientBalance_ThrowsBankException() {
        account.setBalance(new BigDecimal("30.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BankException exception = assertThrows(BankException.class, () -> {
            paymentService.makePayment(1L, paymentRequest, "127.0.0.1");
        });

        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, exception.getErrorCode());
        verify(accountRepository).findById(1L);
        verify(accountRepository, never()).findByIdForUpdate(anyLong());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(providerSimulationService, never()).simulatePayment(any(), any(), any());
    }

    @Test
    void makePayment_AccountNotFound_ThrowsBankException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        BankException exception = assertThrows(BankException.class, () -> {
            paymentService.makePayment(1L, paymentRequest, "127.0.0.1");
        });

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
        verify(accountRepository).findById(1L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void makePayment_AccountNotOwned_ThrowsBankException() {
        User anotherUser = User.builder()
                .id(2L)
                .firstName("Nicat")
                .lastName("Aliyev")
                .email("nicat.aliyev@hkbank.az")
                .password("$2a$10$encodedPassword")
                .phoneNumber("+994501234568")
                .role(Role.USER)
                .build();
        account.setUser(anotherUser);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BankException exception = assertThrows(BankException.class, () -> {
            paymentService.makePayment(1L, paymentRequest, "127.0.0.1");
        });

        assertEquals(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS, exception.getErrorCode());
        verify(accountRepository).findById(1L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void makePayment_AmountExceedsLimit_ThrowsBankException() {
        paymentRequest.setAmount(new BigDecimal("600.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BankException exception = assertThrows(BankException.class, () -> {
            paymentService.makePayment(1L, paymentRequest, "127.0.0.1");
        });

        assertEquals(ErrorCode.PAYMENT_LIMIT_EXCEEDED, exception.getErrorCode());
        verify(accountRepository).findById(1L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void makePayment_ProviderFailed_RestoresBalance() {
        BigDecimal originalBalance = account.getBalance();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(providerSimulationService.simulatePayment(
                eq(ProviderType.MOBILE), eq("+994501234567"), any(BigDecimal.class)))
                .thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(auditService).log(anyLong(), anyString(), anyString(), anyString());

        BankException exception = assertThrows(BankException.class, () -> {
            paymentService.makePayment(1L, paymentRequest, "127.0.0.1");
        });

        assertEquals(ErrorCode.PROVIDER_REJECTED, exception.getErrorCode());
        assertEquals(originalBalance, account.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.FAILED));
        verify(auditService).log(eq(1L), anyString(), contains("rejected by provider"), anyString());
    }

    @Test
    void getPaymentById_Success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toPaymentResponse(payment)).thenReturn(paymentResponse);

        PaymentResponse response = paymentService.getPaymentById(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("PAY12345678", response.getReferenceNumber());

        verify(paymentRepository).findById(1L);
        verify(paymentMapper).toPaymentResponse(payment);
    }

    @Test
    void getPaymentById_NotOwner_ThrowsBankException() {
        User anotherUser = User.builder()
                .id(2L)
                .firstName("Tural")
                .lastName("Hasanov")
                .email("tural.hasanov@hkbank.az")
                .build();
        account.setUser(anotherUser);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        BankException exception = assertThrows(BankException.class, () -> {
            paymentService.getPaymentById(1L, 1L);
        });

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(paymentRepository).findById(1L);
        verify(paymentMapper, never()).toPaymentResponse(any());
    }

    @Test
    void getUserPayments_Success() {
        List<Payment> payments = Arrays.asList(payment, payment);
        PaymentSummaryResponse summaryResponse = PaymentSummaryResponse.builder()
                .id(1L)
                .referenceNumber("PAY12345678")
                .providerType(ProviderType.MOBILE)
                .providerName("Azercell")
                .amount(new BigDecimal("50.00"))
                .status(PaymentStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findByAccountUserId(1L)).thenReturn(payments);
        when(paymentMapper.toPaymentSummaryResponse(any(Payment.class))).thenReturn(summaryResponse);

        List<PaymentSummaryResponse> responses = paymentService.getUserPayments(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());

        verify(paymentRepository).findByAccountUserId(1L);
        verify(paymentMapper, times(2)).toPaymentSummaryResponse(any(Payment.class));
    }

    @Test
    void makePayment_AccountBlocked_ThrowsBankException() {
        account.setStatus(AccountStatus.BLOCKED);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BankException exception = assertThrows(BankException.class, () -> {
            paymentService.makePayment(1L, paymentRequest, "127.0.0.1");
        });

        assertEquals(ErrorCode.ACCOUNT_BLOCKED, exception.getErrorCode());
        verify(accountRepository).findById(1L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void makePayment_AccountClosed_ThrowsBankException() {
        account.setStatus(AccountStatus.CLOSED);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BankException exception = assertThrows(BankException.class, () -> {
            paymentService.makePayment(1L, paymentRequest, "127.0.0.1");
        });

        assertEquals(ErrorCode.ACCOUNT_BLOCKED, exception.getErrorCode());
        verify(accountRepository).findById(1L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void getPaymentByReference_Success() {
        when(paymentRepository.findByReferenceNumber("PAY12345678")).thenReturn(Optional.of(payment));
        when(paymentMapper.toPaymentResponse(payment)).thenReturn(paymentResponse);

        PaymentResponse response = paymentService.getPaymentByReference("PAY12345678");

        assertNotNull(response);
        assertEquals("PAY12345678", response.getReferenceNumber());

        verify(paymentRepository).findByReferenceNumber("PAY12345678");
        verify(paymentMapper).toPaymentResponse(payment);
    }

    @Test
    void getPaymentByReference_NotFound_ThrowsBankException() {
        when(paymentRepository.findByReferenceNumber("INVALID")).thenReturn(Optional.empty());

        BankException exception = assertThrows(BankException.class, () -> {
            paymentService.getPaymentByReference("INVALID");
        });

        assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
        verify(paymentRepository).findByReferenceNumber("INVALID");
    }

    @Test
    void getPaymentsByAccount_Success() {
        List<Payment> payments = Arrays.asList(payment);
        PaymentSummaryResponse summaryResponse = PaymentSummaryResponse.builder()
                .id(1L)
                .referenceNumber("PAY12345678")
                .providerType(ProviderType.MOBILE)
                .providerName("Azercell")
                .amount(new BigDecimal("50.00"))
                .status(PaymentStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(paymentRepository.findByAccountId(1L)).thenReturn(payments);
        when(paymentMapper.toPaymentSummaryResponse(any(Payment.class))).thenReturn(summaryResponse);

        List<PaymentSummaryResponse> responses = paymentService.getPaymentsByAccount(1L, 1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());

        verify(accountRepository).findById(1L);
        verify(paymentRepository).findByAccountId(1L);
        verify(paymentMapper).toPaymentSummaryResponse(any(Payment.class));
    }

    @Test
    void getPaymentsByAccount_AccountNotOwned_ThrowsBankException() {
        User anotherUser = User.builder()
                .id(2L)
                .firstName("Kamran")
                .lastName("Quliyev")
                .build();
        account.setUser(anotherUser);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BankException exception = assertThrows(BankException.class, () -> {
            paymentService.getPaymentsByAccount(1L, 1L);
        });

        assertEquals(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS, exception.getErrorCode());
        verify(accountRepository).findById(1L);
        verify(paymentRepository, never()).findByAccountId(anyLong());
    }
}
