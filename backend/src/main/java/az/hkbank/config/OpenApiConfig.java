package az.hkbank.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for the HK Bank System.
 * Configures API documentation and JWT authentication scheme.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "HK Bank System API",
                version = "1.0.0",
                description = "Digital Banking & Transaction Engine REST API",
                contact = @Contact(
                        name = "HK Bank",
                        email = "support@hkbank.az"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Development Server"),
                @Server(url = "https://api.hkbank.az", description = "Production Server")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
