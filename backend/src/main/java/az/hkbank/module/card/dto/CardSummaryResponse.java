package az.hkbank.module.card.dto;

import az.hkbank.module.card.entity.CardStatus;
import az.hkbank.module.card.entity.CardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for lightweight card summary responses.
 * Used in list operations to reduce data transfer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardSummaryResponse {

    private Long id;
    private String maskedCardNumber;
    private String cardHolder;
    private CardType cardType;
    private CardStatus status;
    private LocalDate expiryDate;
}
