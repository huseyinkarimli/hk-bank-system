package az.hkbank.module.admin;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.account.dto.AccountResponse;
import az.hkbank.module.account.entity.Account;
import az.hkbank.module.account.entity.AccountStatus;
import az.hkbank.module.account.entity.CurrencyType;
import az.hkbank.module.account.mapper.AccountMapper;
import az.hkbank.module.account.repository.AccountRepository;
import az.hkbank.module.admin.dto.AdminFundRequest;
import az.hkbank.module.audit.service.AuditAction;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.card.repository.CardRepository;
import az.hkbank.module.notification.service.NotificationService;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for admin funding flows implemented in {@link TransactionServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

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

    private User owner;
    private Account account;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(10L)
                .firstName("Admin")
                .lastName("Owner")
                .email("owner@hkbank.az")
                .password("x")
                .phoneNumber("+994000000000")
                .role(Role.USER)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        account = Account.builder()
                .id(5L)
                .accountNumber("1111111111")
                .iban("AZ00HKBA00000000001111111111")
                .balance(new BigDecimal("200.00"))
                .currencyType(CurrencyType.AZN)
                .status(AccountStatus.ACTIVE)
                .user(owner)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void adminDeposit_success() {
        when(accountRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction tx = inv.getArgument(0);
            tx.setId(100L);
            if (tx.getReferenceNumber() == null) {
                tx.setReferenceNumber("TXNADMINDP");
            }
            return tx;
        });
        when(accountMapper.toAccountResponse(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            return AccountResponse.builder()
                    .id(a.getId())
                    .balance(a.getBalance())
                    .currencyType(a.getCurrencyType())
                    .accountNumber(a.getAccountNumber())
                    .iban(a.getIban())
                    .status(a.getStatus())
                    .createdAt(a.getCreatedAt())
                    .build();
        });

        AdminFundRequest request = AdminFundRequest.builder()
                .amount(new BigDecimal("50.00"))
                .description("Correction")
                .build();

        AccountResponse response = transactionService.adminDeposit(5L, request, "127.0.0.1");

        assertEquals(0, new BigDecimal("250.00").compareTo(response.getBalance()));

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        Transaction saved = txCaptor.getValue();
        assertEquals(TransactionType.DEPOSIT, saved.getType());
        assertEquals(TransactionStatus.SUCCESS, saved.getStatus());
        assertNull(saved.getSenderAccount());
        assertSame(account, saved.getReceiverAccount());
        assertEquals("Admin deposit: Correction", saved.getDescription());
        assertEquals(0, new BigDecimal("50.00").compareTo(saved.getAmount()));

        verify(auditService).log(
                eq(10L),
                eq(AuditAction.ADMIN_DEPOSIT),
                contains("TXNADMINDP"),
                eq("127.0.0.1")
        );
    }

    @Test
    void adminDeposit_accountNotFound() {
        when(accountRepository.findByIdForUpdate(5L)).thenReturn(Optional.empty());
        AdminFundRequest request = AdminFundRequest.builder()
                .amount(BigDecimal.TEN)
                .description("x")
                .build();

        BankException ex = assertThrows(BankException.class,
                () -> transactionService.adminDeposit(5L, request, "127.0.0.1"));
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, ex.getErrorCode());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void adminDeposit_deletedAccount() {
        account.setDeleted(true);
        when(accountRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(account));

        AdminFundRequest request = AdminFundRequest.builder()
                .amount(BigDecimal.TEN)
                .description("x")
                .build();

        assertThrows(BankException.class,
                () -> transactionService.adminDeposit(5L, request, "127.0.0.1"));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void adminWithdraw_success() {
        when(accountRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction tx = inv.getArgument(0);
            tx.setId(101L);
            if (tx.getReferenceNumber() == null) {
                tx.setReferenceNumber("TXNADMINWD");
            }
            return tx;
        });
        when(accountMapper.toAccountResponse(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            return AccountResponse.builder()
                    .id(a.getId())
                    .balance(a.getBalance())
                    .currencyType(a.getCurrencyType())
                    .accountNumber(a.getAccountNumber())
                    .iban(a.getIban())
                    .status(a.getStatus())
                    .createdAt(a.getCreatedAt())
                    .build();
        });

        AdminFundRequest request = AdminFundRequest.builder()
                .amount(new BigDecimal("75.00"))
                .description("Fee reversal")
                .build();

        AccountResponse response = transactionService.adminWithdraw(5L, request, "10.0.0.1");

        assertEquals(0, new BigDecimal("125.00").compareTo(response.getBalance()));

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        Transaction saved = txCaptor.getValue();
        assertEquals(TransactionType.WITHDRAWAL, saved.getType());
        assertEquals(TransactionStatus.SUCCESS, saved.getStatus());
        assertNull(saved.getReceiverAccount());
        assertSame(account, saved.getSenderAccount());
        assertEquals("Admin withdrawal: Fee reversal", saved.getDescription());

        verify(auditService).log(
                eq(10L),
                eq(AuditAction.ADMIN_WITHDRAWAL),
                contains("TXNADMINWD"),
                eq("10.0.0.1")
        );
    }

    @Test
    void adminWithdraw_insufficientBalance() {
        when(accountRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(account));

        AdminFundRequest request = AdminFundRequest.builder()
                .amount(new BigDecimal("500.00"))
                .description("too much")
                .build();

        BankException ex = assertThrows(BankException.class,
                () -> transactionService.adminWithdraw(5L, request, "127.0.0.1"));
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, ex.getErrorCode());
        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
}
