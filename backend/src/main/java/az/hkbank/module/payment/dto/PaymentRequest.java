package az.hkbank.module.payment.dto;

import az.hkbank.module.payment.entity.ProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a utility payment.
 * Contains all necessary information to process a payment to a utility provider.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotNull(message = "Provider type is required")
    private ProviderType providerType;

    @NotBlank(message = "Provider name is required")
    private String providerName;

    @NotBlank(message = "Subscriber number is required")
    private String subscriberNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String description;
}
