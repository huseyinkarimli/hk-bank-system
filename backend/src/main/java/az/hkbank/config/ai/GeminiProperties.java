package az.hkbank.config.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Google Gemini API integration.
 * Binds properties from application.yml with prefix "app.ai.gemini".
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.ai.gemini")
public class GeminiProperties {

    private String apiKey;
    private String apiUrl;
    private Integer maxTokens;
    private Double temperature;
}
