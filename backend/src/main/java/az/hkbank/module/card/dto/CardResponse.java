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
 * PAN is returned formatted for the card owner; masked variant included for display defaults.
 * CVV is returned only when stored in plain form (demo); PIN is never returned.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {

    private Long id;
    private String maskedCardNumber;
    /** Full 16-digit PAN, spaced for display (owner-only endpoints). */
    private String fullCardNumber;
    /** Plain CVV when available (not returned for legacy bcrypt-stored values). */
    private String cvv;
    private String cardHolder;
    private LocalDate expiryDate;
    private CardType cardType;
    private CardStatus status;
    private Long accountId;
    private LocalDateTime createdAt;
}
