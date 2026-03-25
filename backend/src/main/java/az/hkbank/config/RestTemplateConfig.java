package az.hkbank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for RestTemplate bean.
 * Provides centrally configured RestTemplate for HTTP client operations.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates RestTemplate bean for HTTP client operations.
     *
     * @return configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
