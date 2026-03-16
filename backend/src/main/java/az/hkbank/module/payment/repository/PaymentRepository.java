package az.hkbank.module.payment.repository;

import az.hkbank.module.payment.entity.Payment;
import az.hkbank.module.payment.entity.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity operations.
 * Provides database access methods for payment management.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds all payments for a specific user.
     *
     * @param userId the user ID
     * @return list of payments
     */
    @Query("SELECT p FROM Payment p WHERE p.account.user.id = :userId ORDER BY p.createdAt DESC")
    List<Payment> findByAccountUserId(@Param("userId") Long userId);

    /**
     * Finds all payments for a specific account.
     *
     * @param accountId the account ID
     * @return list of payments
     */
    @Query("SELECT p FROM Payment p WHERE p.account.id = :accountId ORDER BY p.createdAt DESC")
    List<Payment> findByAccountId(@Param("accountId") Long accountId);

    /**
     * Finds a payment by reference number.
     *
     * @param referenceNumber the reference number
     * @return Optional containing the payment if found
     */
    Optional<Payment> findByReferenceNumber(String referenceNumber);

    /**
     * Finds all payments for a user filtered by provider type.
     *
     * @param userId the user ID
     * @param providerType the provider type
     * @return list of payments
     */
    @Query("SELECT p FROM Payment p WHERE p.account.user.id = :userId AND p.providerType = :providerType ORDER BY p.createdAt DESC")
    List<Payment> findByAccountUserIdAndProviderType(@Param("userId") Long userId, @Param("providerType") ProviderType providerType);

    /**
     * Calculates the sum of payment amounts for an account within a date range.
     *
     * @param accountId the account ID
     * @param startDate the start date
     * @param endDate the end date
     * @return sum of payment amounts, or zero if no payments found
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.account.id = :accountId AND p.status = 'SUCCESS' AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByAccountIdAndDateRange(@Param("accountId") Long accountId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
