package az.hkbank.module.transaction.service.impl;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.account.entity.CurrencyType;
import az.hkbank.module.transaction.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Implementation of ExchangeRateService.
 * Fetches real-time exchange rates from exchangerate-api.com with caching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

    @Value("${app.exchange-api.url:https://v6.exchangerate-api.com/v6}")
    private String exchangeApiUrl;

    @Value("${app.exchange-api.key:demo}")
    private String exchangeApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Cacheable(value = "exchangeRates", key = "#from + '_' + #to")
    public BigDecimal getExchangeRate(CurrencyType from, CurrencyType to) {
        if (from == to) {
            return BigDecimal.ONE;
        }

        log.info("Fetching exchange rate: {} -> {}", from, to);

        try {
            String url = String.format("%s/%s/pair/%s/%s",
                    exchangeApiUrl, exchangeApiKey, from.name(), to.name());

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"success".equals(response.get("result"))) {
                log.error("Exchange rate API returned error for {} -> {}", from, to);
                throw new BankException(ErrorCode.CURRENCY_NOT_SUPPORTED,
                        "Failed to fetch exchange rate for " + from + " to " + to);
            }

            Object conversionRate = response.get("conversion_rate");
            BigDecimal rate = new BigDecimal(conversionRate.toString());

            log.info("Exchange rate fetched: {} -> {} = {}", from, to, rate);

            return rate.setScale(6, RoundingMode.HALF_UP);

        } catch (Exception e) {
            log.error("Failed to fetch exchange rate: {} -> {}", from, to, e);
            throw new BankException(ErrorCode.CURRENCY_NOT_SUPPORTED,
                    "Currency exchange service unavailable");
        }
    }

    @Override
    public BigDecimal convert(BigDecimal amount, CurrencyType from, CurrencyType to) {
        if (from == to) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal rate = getExchangeRate(from, to);
        BigDecimal converted = amount.multiply(rate);

        log.info("Currency conversion: {} {} -> {} {} (rate: {})",
                amount, from, converted.setScale(2, RoundingMode.HALF_UP), to, rate);

        return converted.setScale(2, RoundingMode.HALF_UP);
    }
}
