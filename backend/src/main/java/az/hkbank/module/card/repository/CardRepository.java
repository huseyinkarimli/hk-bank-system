package az.hkbank.module.card.repository;

import az.hkbank.module.card.entity.Card;
import az.hkbank.module.card.entity.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Card entity operations.
 * Provides database access methods for card management.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    /**
     * Finds all cards for a specific account.
     *
     * @param accountId the account ID
     * @return list of cards
     */
    List<Card> findByAccountId(Long accountId);

    /**
     * Finds all cards for a specific user (through account relationship).
     *
     * @param userId the user ID
     * @return list of cards
     */
    @Query("SELECT c FROM Card c WHERE c.account.user.id = :userId")
    List<Card> findByAccountUserId(@Param("userId") Long userId);

    /**
     * Checks if a card exists with the given card number.
     *
     * @param cardNumber the card number
     * @return true if exists, false otherwise
     */
    boolean existsByCardNumber(String cardNumber);

    /**
     * Finds a card by card number.
     *
     * @param cardNumber the card number
     * @return Optional containing the card if found
     */
    Optional<Card> findByCardNumber(String cardNumber);

    /**
     * Finds all active (non-deleted) cards for a specific account.
     *
     * @param accountId the account ID
     * @return list of active cards
     */
    @Query("SELECT c FROM Card c WHERE c.account.id = :accountId AND c.isDeleted = false")
    List<Card> findActiveCardsByAccountId(@Param("accountId") Long accountId);

    /**
     * Counts cards for an account excluding a specific status.
     *
     * @param accountId the account ID
     * @param status the status to exclude
     * @return count of cards
     */
    int countByAccountIdAndStatusNot(Long accountId, CardStatus status);

    /**
     * Finds all active cards (admin operation).
     *
     * @return list of all active cards
     */
    @Query("SELECT c FROM Card c WHERE c.isDeleted = false")
    List<Card> findAllActive();

    long countByStatus(CardStatus status);
}
