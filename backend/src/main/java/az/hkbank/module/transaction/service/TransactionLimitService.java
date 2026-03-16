package az.hkbank.module.transaction.service;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.account.entity.CurrencyType;
import az.hkbank.module.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service for enforcing transaction limits.
 * Validates single transaction and daily spending limits.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionLimitService {

    private static final BigDecimal SINGLE_TRANSACTION_LIMIT_AZN = new BigDecimal("10000.00");
    private static final BigDecimal DAILY_LIMIT_AZN = new BigDecimal("50000.00");

    private final TransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;

    /**
     * Checks if a transaction exceeds limits.
     * Single transaction max: 10,000 AZN equivalent
     * Daily limit: 50,000 AZN equivalent
     *
     * @param userId the user ID
     * @param amount the transaction amount
     * @param currency the transaction currency
     * @throws BankException if limits are exceeded
     */
    public void checkTransactionLimit(Long userId, BigDecimal amount, CurrencyType currency) {
        BigDecimal amountInAzn = convertToAzn(amount, currency);

        if (amountInAzn.compareTo(SINGLE_TRANSACTION_LIMIT_AZN) > 0) {
            log.warn("Single transaction limit exceeded for user {}: {} AZN", userId, amountInAzn);
            throw new BankException(ErrorCode.DAILY_LIMIT_EXCEEDED,
                    "Single transaction limit exceeded. Maximum: " + SINGLE_TRANSACTION_LIMIT_AZN + " AZN");
        }

        BigDecimal dailySpent = getDailySpent(userId);
        BigDecimal totalAfterTransaction = dailySpent.add(amountInAzn);

        if (totalAfterTransaction.compareTo(DAILY_LIMIT_AZN) > 0) {
            log.warn("Daily limit exceeded for user {}: {} AZN (spent: {}, new: {})",
                    userId, totalAfterTransaction, dailySpent, amountInAzn);
            throw new BankException(ErrorCode.DAILY_LIMIT_EXCEEDED,
                    "Daily transaction limit exceeded. Limit: " + DAILY_LIMIT_AZN +
                            " AZN, Spent today: " + dailySpent + " AZN");
        }

        log.debug("Transaction limit check passed for user {}: {} AZN (daily spent: {})",
                userId, amountInAzn, dailySpent);
    }

    /**
     * Calculates total amount spent by user today in AZN equivalent.
     *
     * @param userId the user ID
     * @return total spent in AZN
     */
    public BigDecimal getDailySpent(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        BigDecimal dailySpent = transactionRepository
                .sumAmountByUserIdAndDateRange(userId, startOfDay, now);

        return dailySpent != null ? dailySpent : BigDecimal.ZERO;
    }

    /**
     * Converts amount to AZN for limit checking.
     *
     * @param amount the amount
     * @param currency the currency
     * @return amount in AZN
     */
    private BigDecimal convertToAzn(BigDecimal amount, CurrencyType currency) {
        if (currency == CurrencyType.AZN) {
            return amount;
        }
        return exchangeRateService.convert(amount, currency, CurrencyType.AZN);
    }
}
