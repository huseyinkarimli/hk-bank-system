package az.hkbank.module.payment.service;

import az.hkbank.module.payment.entity.ProviderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Service for simulating external utility provider API calls.
 * Simulates real-world provider behavior with validation and success rates.
 */
@Slf4j
@Service
public class ProviderSimulationService {

    private static final Pattern MOBILE_PHONE_PATTERN = Pattern.compile("^\\+994[0-9]{9}$");
    private static final double SUCCESS_RATE = 0.95;
    private static final Random random = new Random();

    private static final Map<ProviderType, List<String>> PROVIDERS = new HashMap<>();

    static {
        PROVIDERS.put(ProviderType.MOBILE, Arrays.asList("Azercell", "Bakcell", "Nar Mobile"));
        PROVIDERS.put(ProviderType.INTERNET, Arrays.asList("AzTelecom", "Catel", "Ultel"));
        PROVIDERS.put(ProviderType.ELECTRICITY, Collections.singletonList("Azerenerji"));
        PROVIDERS.put(ProviderType.WATER, Collections.singletonList("Azersu"));
        PROVIDERS.put(ProviderType.GAS, Collections.singletonList("Azerigas"));
        PROVIDERS.put(ProviderType.TV, Arrays.asList("Aztv", "CBC", "ITV"));
        PROVIDERS.put(ProviderType.OTHER, Collections.singletonList("Custom"));
    }

    /**
     * Simulates a payment to an external utility provider.
     * Validates input and returns success/failure based on simulated provider response.
     *
     * @param providerType the type of provider
     * @param subscriberNumber the subscriber/account number
     * @param amount the payment amount
     * @return true if payment successful, false if provider rejected
     */
    public boolean simulatePayment(ProviderType providerType, String subscriberNumber, BigDecimal amount) {
        log.info("Simulating payment to provider type: {}, subscriber: {}, amount: {}",
                providerType, subscriberNumber, amount);

        if (subscriberNumber == null || subscriberNumber.isBlank()) {
            log.warn("Payment simulation failed: blank subscriber number");
            return false;
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Payment simulation failed: invalid amount");
            return false;
        }

        if (providerType == ProviderType.MOBILE) {
            if (!MOBILE_PHONE_PATTERN.matcher(subscriberNumber).matches()) {
                log.warn("Payment simulation failed: invalid mobile phone format");
                return false;
            }
        }

        boolean success = random.nextDouble() < SUCCESS_RATE;
        log.info("Payment simulation result: {}", success ? "SUCCESS" : "FAILED");

        return success;
    }

    /**
     * Gets the list of available providers for a specific provider type.
     *
     * @param providerType the provider type
     * @return list of provider names
     */
    public List<String> getProvidersByType(ProviderType providerType) {
        return PROVIDERS.getOrDefault(providerType, Collections.emptyList());
    }

    /**
     * Gets all available providers grouped by type.
     *
     * @return map of provider types to provider names
     */
    public Map<ProviderType, List<String>> getAllProviders() {
        return new HashMap<>(PROVIDERS);
    }
}
