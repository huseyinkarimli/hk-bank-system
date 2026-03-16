package az.hkbank.module.account.service;

import az.hkbank.module.account.dto.*;
import az.hkbank.module.account.entity.CurrencyType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service interface for account management operations.
 * Defines business logic for account creation, retrieval, and management.
 */
public interface AccountService {

    /**
     * Creates a new account for a user.
     *
     * @param userId the user ID
     * @param request the account creation request
     * @return created account response
     */
    AccountResponse createAccount(Long userId, CreateAccountRequest request);

    /**
     * Retrieves an account by ID for a specific user.
     *
     * @param accountId the account ID
     * @param userId the user ID
     * @return account response
     */
    AccountResponse getAccountById(Long accountId, Long userId);

    /**
     * Retrieves all accounts for a user.
     *
     * @param userId the user ID
     * @return list of account summaries
     */
    List<AccountSummaryResponse> getUserAccounts(Long userId);

    /**
     * Retrieves an account by IBAN.
     *
     * @param iban the IBAN
     * @return account response
     */
    AccountResponse getAccountByIban(String iban);

    /**
     * Updates account status (admin only).
     *
     * @param accountId the account ID
     * @param request the status update request
     * @return updated account response
     */
    AccountResponse updateAccountStatus(Long accountId, UpdateAccountStatusRequest request);

    /**
     * Soft deletes an account.
     *
     * @param accountId the account ID
     * @param userId the user ID
     */
    void softDeleteAccount(Long accountId, Long userId);

    /**
     * Retrieves all accounts (admin only).
     *
     * @return list of all account summaries
     */
    List<AccountSummaryResponse> getAllAccounts();

    /**
     * Calculates total balance per currency for a user.
     *
     * @param userId the user ID
     * @return map of currency type to total balance
     */
    Map<CurrencyType, BigDecimal> getTotalBalanceByUser(Long userId);
}
