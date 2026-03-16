package az.hkbank.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration for the HK Bank System.
 * Configures Caffeine cache for exchange rates with 1-hour TTL.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configures Caffeine cache manager for exchange rates.
     * Exchange rates are cached for 1 hour to reduce external API calls.
     *
     * @return configured cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("exchangeRates");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(100)
                .recordStats());
        return cacheManager;
    }
}
