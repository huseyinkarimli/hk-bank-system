package az.hkbank.module.admin;

import az.hkbank.module.account.entity.Account;
import az.hkbank.module.account.entity.AccountStatus;
import az.hkbank.module.account.entity.CurrencyType;
import az.hkbank.module.account.repository.AccountRepository;
import az.hkbank.module.admin.dto.AdminDashboardStatsResponse;
import az.hkbank.module.admin.service.AdminDashboardService;
import az.hkbank.module.card.entity.CardStatus;
import az.hkbank.module.card.repository.CardRepository;
import az.hkbank.module.transaction.repository.TransactionRepository;
import az.hkbank.module.transaction.service.ExchangeRateService;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AdminDashboardService}.
 */
@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    void getDashboardStats_aggregatesRepositoriesAndConvertsToAzn() {
        when(userRepository.countByIsDeletedFalse()).thenReturn(100L);
        when(userRepository.countByIsDeletedTrue()).thenReturn(5L);
        when(cardRepository.countByStatus(CardStatus.ACTIVE)).thenReturn(40L);
        when(cardRepository.countByStatus(CardStatus.BLOCKED)).thenReturn(3L);
        when(transactionRepository.countByCreatedAtBetween(any(), any())).thenReturn(12L);
        when(transactionRepository.sumAmountByCreatedAtBetween(any(), any()))
                .thenReturn(new BigDecimal("1500.50"));

        User owner = User.builder().id(1L).build();
        Account aznAcc = Account.builder()
                .id(1L)
                .accountNumber("1000000001")
                .iban("AZ00HKBA00000000001000000001")
                .balance(new BigDecimal("1000.00"))
                .currencyType(CurrencyType.AZN)
                .status(AccountStatus.ACTIVE)
                .user(owner)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Account usdAcc = Account.builder()
                .id(2L)
                .accountNumber("1000000002")
                .iban("AZ00HKBA00000000001000000002")
                .balance(new BigDecimal("100.00"))
                .currencyType(CurrencyType.USD)
                .status(AccountStatus.ACTIVE)
                .user(owner)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(accountRepository.findAllActive()).thenReturn(List.of(aznAcc, usdAcc));
        when(exchangeRateService.getExchangeRate(CurrencyType.USD, CurrencyType.AZN))
                .thenReturn(new BigDecimal("1.700000"));

        AdminDashboardStatsResponse stats = adminDashboardService.getDashboardStats();

        assertEquals(100L, stats.getTotalUsers());
        assertEquals(5L, stats.getBannedUsers());
        assertEquals(40L, stats.getTotalActiveCards());
        assertEquals(3L, stats.getTotalBlockedCards());
        assertEquals(12L, stats.getTodayTransactionCount());
        assertEquals(0, new BigDecimal("1500.50").compareTo(stats.getTodayTransactionVolume()));
        assertEquals(0, new BigDecimal("1170.00").compareTo(stats.getTotalBalanceInAzn()));
        assertNotNull(stats.getGeneratedAt());

        verify(exchangeRateService).getExchangeRate(eq(CurrencyType.USD), eq(CurrencyType.AZN));
        verify(exchangeRateService, never()).getExchangeRate(eq(CurrencyType.AZN), any());
    }
}
