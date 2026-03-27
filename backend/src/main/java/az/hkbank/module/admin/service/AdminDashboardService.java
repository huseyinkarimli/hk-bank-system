package az.hkbank.module.admin.service;

import az.hkbank.module.account.entity.Account;
import az.hkbank.module.account.entity.CurrencyType;
import az.hkbank.module.account.repository.AccountRepository;
import az.hkbank.module.admin.dto.AdminDashboardStatsResponse;
import az.hkbank.module.card.entity.CardStatus;
import az.hkbank.module.card.repository.CardRepository;
import az.hkbank.module.transaction.repository.TransactionRepository;
import az.hkbank.module.transaction.service.ExchangeRateService;
import az.hkbank.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Read-only aggregated statistics for the admin dashboard.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ExchangeRateService exchangeRateService;

    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getDashboardStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        long totalUsers = userRepository.countByIsDeletedFalse();
        long bannedUsers = userRepository.countByIsDeletedTrue();
        long totalActiveCards = cardRepository.countByStatus(CardStatus.ACTIVE);
        long totalBlockedCards = cardRepository.countByStatus(CardStatus.BLOCKED);
        long todayTransactionCount = transactionRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        BigDecimal todayTransactionVolume = transactionRepository
                .sumAmountByCreatedAtBetween(startOfDay, endOfDay);

        BigDecimal totalBalanceInAzn = computeTotalBalanceInAzn();

        return AdminDashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .bannedUsers(bannedUsers)
                .totalActiveCards(totalActiveCards)
                .totalBlockedCards(totalBlockedCards)
                .todayTransactionCount(todayTransactionCount)
                .todayTransactionVolume(todayTransactionVolume)
                .totalBalanceInAzn(totalBalanceInAzn)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private BigDecimal computeTotalBalanceInAzn() {
        List<Account> accounts = accountRepository.findAllActive();
        Map<CurrencyType, BigDecimal> sumByCurrency = accounts.stream()
                .collect(Collectors.groupingBy(
                        Account::getCurrencyType,
                        Collectors.reducing(BigDecimal.ZERO, Account::getBalance, BigDecimal::add)
                ));

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<CurrencyType, BigDecimal> entry : sumByCurrency.entrySet()) {
            CurrencyType currency = entry.getKey();
            BigDecimal balance = entry.getValue();
            if (currency == CurrencyType.AZN) {
                total = total.add(balance);
            } else {
                try {
                    BigDecimal rate = exchangeRateService.getExchangeRate(currency, CurrencyType.AZN);
                    total = total.add(balance.multiply(rate));
                } catch (Exception e) {
                    log.warn("Exchange rate unavailable for {} → AZN; using 1:1 fallback for dashboard total", currency, e);
                    total = total.add(balance);
                }
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
