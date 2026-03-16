package az.hkbank.module.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    private String sourceCardNumber;

    @NotBlank(message = "Target card number is required")
    private String targetCardNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String description;
}
