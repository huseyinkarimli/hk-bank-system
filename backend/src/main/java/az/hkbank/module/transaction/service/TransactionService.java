package az.hkbank.module.transaction.service;

import az.hkbank.module.account.dto.AccountResponse;
import az.hkbank.module.admin.dto.AdminFundRequest;
import az.hkbank.module.transaction.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for transaction operations.
 * Defines business logic for money transfers and transaction management.
 */
public interface TransactionService {

    /**
     * Transfers money between cards (P2P card transfer).
     *
     * @param userId the user ID initiating the transfer
     * @param request the transfer request
     * @param ipAddress the client IP address
     * @return transaction response
     */
    TransactionResponse transferByCard(Long userId, P2PCardTransferRequest request, String ipAddress);

    /**
     * Transfers money between accounts using IBAN (P2P IBAN transfer).
     *
     * @param userId the user ID initiating the transfer
     * @param request the transfer request
     * @param ipAddress the client IP address
     * @return transaction response
     */
    TransactionResponse transferByIban(Long userId, P2PIbanTransferRequest request, String ipAddress);

    /**
     * Retrieves a transaction by ID for a specific user.
     *
     * @param id the transaction ID
     * @param userId the user ID
     * @return transaction response
     */
    TransactionResponse getTransactionById(Long id, Long userId);

    /**
     * Retrieves user transactions with optional filters and pagination.
     *
     * @param userId the user ID
     * @param filter the filter criteria
     * @param pageable pagination parameters
     * @return page of transaction summaries
     */
    Page<TransactionSummaryResponse> getUserTransactions(Long userId, TransactionFilterRequest filter, Pageable pageable);

    /**
     * Retrieves a transaction by reference number.
     *
     * @param referenceNumber the reference number
     * @return transaction response
     */
    TransactionResponse getTransactionByReference(String referenceNumber);

    /**
     * Retrieves all transactions (admin only).
     *
     * @param pageable pagination parameters
     * @return page of transaction summaries
     */
    Page<TransactionSummaryResponse> getAllTransactions(Pageable pageable);

    /**
     * Admin credits an account balance and records a DEPOSIT transaction.
     *
     * @param accountId target account ID
     * @param request   amount and description
     * @param ipAddress client IP for audit and transaction record
     * @return updated account
     */
    AccountResponse adminDeposit(Long accountId, AdminFundRequest request, String ipAddress);

    /**
     * Admin debits an account balance and records a WITHDRAWAL transaction.
     *
     * @param accountId source account ID
     * @param request   amount and description
     * @param ipAddress client IP for audit and transaction record
     * @return updated account
     */
    AccountResponse adminWithdraw(Long accountId, AdminFundRequest request, String ipAddress);
}
