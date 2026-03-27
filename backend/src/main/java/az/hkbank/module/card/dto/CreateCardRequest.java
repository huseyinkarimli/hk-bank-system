package az.hkbank.module.card.dto;

import az.hkbank.module.card.entity.CardType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for card creation requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCardRequest {

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotNull(message = "Card type is required")
    private CardType cardType;

    /** Optional 4-digit PIN; defaults to 0000 when omitted (demo / first PIN setup). */
    @Pattern(regexp = "^$|^\\d{4}$", message = "PIN must be exactly 4 digits")
    private String initialPin;
}
