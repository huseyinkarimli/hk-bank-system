package az.hkbank.module.card.dto;

import az.hkbank.module.card.entity.CardStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for card status update requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCardStatusRequest {

    @NotNull(message = "Status is required")
    private CardStatus status;

    private String reason;
}
