package az.hkbank.config.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for JWT token generation and validation.
 * Properties are loaded from application configuration with prefix "app.jwt".
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private Long expiration;
}
