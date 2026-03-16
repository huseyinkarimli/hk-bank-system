package az.hkbank.module.transaction.service;

import az.hkbank.module.account.entity.CurrencyType;

import java.math.BigDecimal;

/**
 * Service interface for currency exchange rate operations.
 * Provides real-time exchange rates and currency conversion.
 */
public interface ExchangeRateService {

    /**
     * Retrieves the exchange rate between two currencies.
     *
     * @param from source currency
     * @param to target currency
     * @return exchange rate
     */
    BigDecimal getExchangeRate(CurrencyType from, CurrencyType to);

    /**
     * Converts an amount from one currency to another.
     *
     * @param amount the amount to convert
     * @param from source currency
     * @param to target currency
     * @return converted amount
     */
    BigDecimal convert(BigDecimal amount, CurrencyType from, CurrencyType to);
}
