package az.hkbank.module.transaction.repository;

import az.hkbank.module.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity operations.
 * Provides database access methods for transaction management.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds all transactions for a sender account.
     *
     * @param accountId the sender account ID
     * @return list of transactions
     */
    List<Transaction> findBySenderAccountId(Long accountId);

    /**
     * Finds all transactions for a receiver account.
     *
     * @param accountId the receiver account ID
     * @return list of transactions
     */
    List<Transaction> findByReceiverAccountId(Long accountId);

    /**
     * Finds all transactions for a user (as sender).
     *
     * @param userId the user ID
     * @return list of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.senderAccount.user.id = :userId")
    List<Transaction> findBySenderAccountUserId(@Param("userId") Long userId);

    /**
     * Finds a transaction by reference number.
     *
     * @param referenceNumber the reference number
     * @return Optional containing the transaction if found
     */
    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    /**
     * Counts transactions from a card after a specific time.
     * Used for fraud detection.
     *
     * @param cardId the sender card ID
     * @param after the start time
     * @return count of transactions
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.senderCard.id = :cardId AND t.createdAt > :after")
    int countBySenderCardIdAndCreatedAtAfter(@Param("cardId") Long cardId, @Param("after") LocalDateTime after);

    /**
     * Sums transaction amounts for a user within a date range.
     * Used for daily limit checking. Converts all to AZN equivalent.
     *
     * @param userId the user ID
     * @param from start date
     * @param to end date
     * @return sum of amounts in AZN
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.senderAccount.user.id = :userId " +
           "AND t.createdAt BETWEEN :from AND :to " +
           "AND t.status = 'SUCCESS' " +
           "AND t.sourceCurrency = 'AZN'")
    BigDecimal sumAmountByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /**
     * Finds transactions for a user within a date range with pagination.
     * Used for transaction statements.
     *
     * @param userId the user ID
     * @param from start date
     * @param to end date
     * @param pageable pagination parameters
     * @return page of transactions
     */
    @Query("SELECT t FROM Transaction t " +
           "WHERE (t.senderAccount.user.id = :userId OR t.receiverAccount.user.id = :userId) " +
           "AND t.createdAt BETWEEN :from AND :to " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    /**
     * Finds all transactions for a user with pagination.
     *
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return page of transactions
     */
    @Query("SELECT t FROM Transaction t " +
           "WHERE t.senderAccount.user.id = :userId OR t.receiverAccount.user.id = :userId " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
