package az.hkbank.module.account.service;

import az.hkbank.module.account.dto.StatementData;

import java.time.LocalDateTime;

/**
 * Service interface for account statement operations.
 * Defines methods for generating account statements in various formats.
 */
public interface StatementService {

    /**
     * Retrieves statement data for an account within a date range.
     *
     * @param accountId the account ID
     * @param userId the user ID requesting the statement
     * @param from the start date
     * @param to the end date
     * @return statement data
     */
    StatementData getStatement(Long accountId, Long userId, LocalDateTime from, LocalDateTime to);

    /**
     * Generates a PDF statement for an account within a date range.
     *
     * @param accountId the account ID
     * @param userId the user ID requesting the statement
     * @param from the start date
     * @param to the end date
     * @return PDF file as byte array
     */
    byte[] generatePdfStatement(Long accountId, Long userId, LocalDateTime from, LocalDateTime to);
}
