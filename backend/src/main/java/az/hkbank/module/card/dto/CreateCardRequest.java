package az.hkbank.module.card.dto;

import az.hkbank.module.card.entity.CardType;
import jakarta.validation.constraints.NotNull;
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
}
