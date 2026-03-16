package az.hkbank.module.card.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for PIN change requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePinRequest {

    @NotBlank(message = "Current PIN is required")
    @Size(min = 4, max = 4, message = "PIN must be exactly 4 digits")
    private String currentPin;

    @NotBlank(message = "New PIN is required")
    @Size(min = 4, max = 4, message = "PIN must be exactly 4 digits")
    private String newPin;
}
