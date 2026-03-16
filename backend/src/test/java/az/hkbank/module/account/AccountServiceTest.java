package az.hkbank.module.account;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.account.dto.AccountResponse;
import az.hkbank.module.account.dto.AccountSummaryResponse;
import az.hkbank.module.account.dto.CreateAccountRequest;
import az.hkbank.module.account.entity.Account;
import az.hkbank.module.account.entity.AccountStatus;
import az.hkbank.module.account.entity.CurrencyType;
import az.hkbank.module.account.mapper.AccountMapper;
import az.hkbank.module.account.repository.AccountRepository;
import az.hkbank.module.account.service.impl.AccountServiceImpl;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.user.entity.Role;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AuditService auditService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User user;
    private Account account;
    private CreateAccountRequest createAccountRequest;
    private AccountResponse accountResponse;
    private AccountSummaryResponse accountSummaryResponse;

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
                .balance(BigDecimal.ZERO)
                .currencyType(CurrencyType.AZN)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createAccountRequest = CreateAccountRequest.builder()
                .currencyType(CurrencyType.AZN)
                .build();

        accountResponse = AccountResponse.builder()
                .id(1L)
                .accountNumber("1234567890")
                .iban("AZ21HKBA00000000001234567890")
                .balance(BigDecimal.ZERO)
                .currencyType(CurrencyType.AZN)
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        accountSummaryResponse = AccountSummaryResponse.builder()
                .id(1L)
                .accountNumber("1234567890")
                .iban("AZ21HKBA00000000001234567890")
                .balance(BigDecimal.ZERO)
                .currencyType(CurrencyType.AZN)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void createAccount_Success_AZN() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserIdAndCurrencyType(1L, CurrencyType.AZN))
                .thenReturn(Optional.empty());
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.existsByIban(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(accountMapper.toAccountResponse(any(Account.class))).thenReturn(accountResponse);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        AccountResponse response = accountService.createAccount(1L, createAccountRequest);

        assertNotNull(response);
        assertEquals("1234567890", response.getAccountNumber());
        assertEquals("AZ21HKBA00000000001234567890", response.getIban());
        assertEquals(CurrencyType.AZN, response.getCurrencyType());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());

        verify(userRepository).findById(1L);
        verify(accountRepository).findByUserIdAndCurrencyType(1L, CurrencyType.AZN);
        verify(accountRepository).save(any(Account.class));
        verify(auditService).log(eq(1L), anyString(), anyString(), anyString());
    }

    @Test
    void createAccount_DuplicateCurrency_ThrowsBankException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserIdAndCurrencyType(1L, CurrencyType.AZN))
                .thenReturn(Optional.of(account));

        BankException exception = assertThrows(BankException.class, () -> {
            accountService.createAccount(1L, createAccountRequest);
        });

        assertEquals(ErrorCode.ACCOUNT_ALREADY_EXISTS, exception.getErrorCode());

        verify(userRepository).findById(1L);
        verify(accountRepository).findByUserIdAndCurrencyType(1L, CurrencyType.AZN);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createAccount_UserNotFound_ThrowsBankException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BankException exception = assertThrows(BankException.class, () -> {
            accountService.createAccount(1L, createAccountRequest);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

        verify(userRepository).findById(1L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getAccountById_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountMapper.toAccountResponse(account)).thenReturn(accountResponse);

        AccountResponse response = accountService.getAccountById(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("AZ21HKBA00000000001234567890", response.getIban());

        verify(accountRepository).findById(1L);
    }

    @Test
    void getAccountById_NotOwner_ThrowsBankException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BankException exception = assertThrows(BankException.class, () -> {
            accountService.getAccountById(1L, 999L);
        });

        assertEquals(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS, exception.getErrorCode());

        verify(accountRepository).findById(1L);
    }

    @Test
    void getUserAccounts_Success() {
        List<Account> accounts = List.of(account);
        when(accountRepository.findByUserIdAndIsDeletedFalse(1L)).thenReturn(accounts);
        when(accountMapper.toAccountSummaryResponse(account)).thenReturn(accountSummaryResponse);

        List<AccountSummaryResponse> response = accountService.getUserAccounts(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("AZ21HKBA00000000001234567890", response.get(0).getIban());

        verify(accountRepository).findByUserIdAndIsDeletedFalse(1L);
    }

    @Test
    void softDeleteAccount_Success() {
        account.setBalance(BigDecimal.ZERO);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        accountService.softDeleteAccount(1L, 1L);

        verify(accountRepository).findById(1L);
        verify(accountRepository).save(argThat(acc -> acc.isDeleted() && acc.getStatus() == AccountStatus.CLOSED));
        verify(auditService).log(eq(1L), anyString(), anyString(), anyString());
    }

    @Test
    void getTotalBalanceByUser_Success() {
        Account aznAccount = Account.builder()
                .id(1L)
                .balance(new BigDecimal("100.00"))
                .currencyType(CurrencyType.AZN)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .isDeleted(false)
                .build();

        Account usdAccount = Account.builder()
                .id(2L)
                .balance(new BigDecimal("50.00"))
                .currencyType(CurrencyType.USD)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .isDeleted(false)
                .build();

        List<Account> accounts = List.of(aznAccount, usdAccount);
        when(accountRepository.findByUserIdAndIsDeletedFalse(1L)).thenReturn(accounts);

        Map<CurrencyType, BigDecimal> balances = accountService.getTotalBalanceByUser(1L);

        assertNotNull(balances);
        assertEquals(2, balances.size());
        assertEquals(new BigDecimal("100.00"), balances.get(CurrencyType.AZN));
        assertEquals(new BigDecimal("50.00"), balances.get(CurrencyType.USD));

        verify(accountRepository).findByUserIdAndIsDeletedFalse(1L);
    }
}
