package az.hkbank.common.exception;

import lombok.Getter;

/**
 * Enumeration of all error codes used in the HK Bank System.
 * Each error code is associated with an HTTP status code and a descriptive message.
 */
@Getter
public enum ErrorCode {

    USER_NOT_FOUND(404, "User not found"),
    USER_ALREADY_EXISTS(409, "User already exists"),
    INVALID_CREDENTIALS(401, "Invalid credentials"),
    
    ACCOUNT_NOT_FOUND(404, "Account not found"),
    INSUFFICIENT_BALANCE(400, "Insufficient balance"),
    
    CARD_NOT_FOUND(404, "Card not found"),
    CARD_BLOCKED(403, "Card is blocked"),
    CARD_FROZEN(403, "Card is frozen"),
    
    TRANSACTION_FAILED(500, "Transaction failed"),
    DAILY_LIMIT_EXCEEDED(400, "Daily limit exceeded"),
    FRAUD_DETECTED(403, "Fraud detected"),
    
    CURRENCY_NOT_SUPPORTED(400, "Currency not supported"),
    
    UNAUTHORIZED(401, "Unauthorized access"),
    FORBIDDEN(403, "Access forbidden"),
    VALIDATION_ERROR(400, "Validation error"),
    INTERNAL_SERVER_ERROR(500, "Internal server error");

    private final int httpStatus;
    private final String message;

    ErrorCode(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
