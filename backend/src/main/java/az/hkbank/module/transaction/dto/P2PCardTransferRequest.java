package az.hkbank.module.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for P2P card transfer requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class P2PCardTransferRequest {

    @NotBlank(message = "Source card number is required")
    @Size(min = 16, max = 16, message = "Card number must be 16 digits")
    private String sourceCardNumber;

    @NotBlank(message = "Target card number is required")
    @Size(min = 16, max = 16, message = "Card number must be 16 digits")
    private String targetCardNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    private String description;
}
