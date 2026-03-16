package az.hkbank.module.account.repository;

import az.hkbank.module.account.entity.Account;
import az.hkbank.module.account.entity.CurrencyType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Account entity operations.
 * Provides database access methods for account management.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Finds all accounts for a specific user.
     *
     * @param userId the user ID
     * @return list of accounts
     */
    List<Account> findByUserId(Long userId);

    /**
     * Finds an account by IBAN.
     *
     * @param iban the IBAN
     * @return Optional containing the account if found
     */
    Optional<Account> findByIban(String iban);

    /**
     * Finds an account by account number.
     *
     * @param accountNumber the account number
     * @return Optional containing the account if found
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Checks if an account exists with the given IBAN.
     *
     * @param iban the IBAN
     * @return true if exists, false otherwise
     */
    boolean existsByIban(String iban);

    /**
     * Checks if an account exists with the given account number.
     *
     * @param accountNumber the account number
     * @return true if exists, false otherwise
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Finds an account for a user with specific currency type.
     *
     * @param userId the user ID
     * @param currencyType the currency type
     * @return Optional containing the account if found
     */
    Optional<Account> findByUserIdAndCurrencyType(Long userId, CurrencyType currencyType);

    /**
     * Finds all active (non-deleted) accounts for a user.
     *
     * @param userId the user ID
     * @return list of active accounts
     */
    List<Account> findByUserIdAndIsDeletedFalse(Long userId);

    /**
     * Finds an account by ID with pessimistic write lock for update operations.
     * Used for balance updates to prevent concurrent modification issues.
     *
     * @param id the account ID
     * @return Optional containing the locked account if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

    /**
     * Finds all active accounts (admin operation).
     *
     * @return list of all active accounts
     */
    @Query("SELECT a FROM Account a WHERE a.isDeleted = false")
    List<Account> findAllActive();
}
