package az.hkbank.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
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
                description = "Professional Digital Banking & Transaction Engine",
                contact = @Contact(
                        name = "Huseyin Karimli",
                        email = "huseyinkarimli.tech@gmail.com"
                ),
                license = @License(
                        name = "MIT",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Development")
        },
        tags = {
                @Tag(name = "Authentication", description = "User registration and login"),
                @Tag(name = "User Management", description = "User profile management"),
                @Tag(name = "Account Management", description = "Bank account operations"),
                @Tag(name = "Card Management", description = "Card operations"),
                @Tag(name = "Transactions", description = "Money transfers"),
                @Tag(name = "Payments", description = "Utility payments"),
                @Tag(name = "Notifications", description = "User notifications"),
                @Tag(name = "Statement", description = "Account statements"),
                @Tag(name = "AI Support", description = "AI-powered customer support"),
                @Tag(name = "Admin", description = "Administrative operations")
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
