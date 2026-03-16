package az.hkbank.module.card.dto;

import az.hkbank.module.card.entity.CardStatus;
import az.hkbank.module.card.entity.CardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for card information responses.
 * Card number is always masked, CVV and PIN are never returned.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {

    private Long id;
    private String maskedCardNumber;
    private String cardHolder;
    private LocalDate expiryDate;
    private CardType cardType;
    private CardStatus status;
    private Long accountId;
    private LocalDateTime createdAt;
}
