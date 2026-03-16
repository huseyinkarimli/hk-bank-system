package az.hkbank.module.card.service;

import az.hkbank.module.card.dto.*;

import java.util.List;

/**
 * Service interface for card management operations.
 * Defines business logic for card creation, retrieval, and management.
 */
public interface CardService {

    /**
     * Creates a new card for a user's account.
     *
     * @param userId the user ID
     * @param request the card creation request
     * @return created card response
     */
    CardResponse createCard(Long userId, CreateCardRequest request);

    /**
     * Retrieves a card by ID for a specific user.
     *
     * @param cardId the card ID
     * @param userId the user ID
     * @return card response
     */
    CardResponse getCardById(Long cardId, Long userId);

    /**
     * Retrieves all cards for a user.
     *
     * @param userId the user ID
     * @return list of card summaries
     */
    List<CardSummaryResponse> getUserCards(Long userId);

    /**
     * Retrieves all cards for a specific account.
     *
     * @param accountId the account ID
     * @param userId the user ID
     * @return list of card summaries
     */
    List<CardSummaryResponse> getCardsByAccount(Long accountId, Long userId);

    /**
     * Updates card status.
     *
     * @param cardId the card ID
     * @param request the status update request
     * @param userId the user ID
     * @return updated card response
     */
    CardResponse updateCardStatus(Long cardId, UpdateCardStatusRequest request, Long userId);

    /**
     * Changes card PIN.
     *
     * @param cardId the card ID
     * @param request the PIN change request
     * @param userId the user ID
     */
    void changePin(Long cardId, ChangePinRequest request, Long userId);

    /**
     * Soft deletes a card.
     *
     * @param cardId the card ID
     * @param userId the user ID
     */
    void softDeleteCard(Long cardId, Long userId);

    /**
     * Retrieves all cards (admin only).
     *
     * @return list of all card summaries
     */
    List<CardSummaryResponse> getAllCards();

    /**
     * Updates card status (admin only).
     *
     * @param cardId the card ID
     * @param request the status update request
     * @return updated card response
     */
    CardResponse adminUpdateCardStatus(Long cardId, UpdateCardStatusRequest request);
}
