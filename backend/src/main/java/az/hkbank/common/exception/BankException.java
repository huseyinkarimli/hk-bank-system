package az.hkbank.common.exception;

import lombok.Getter;

/**
 * Custom runtime exception for the HK Bank System.
 * Wraps an ErrorCode and optional detail message for business logic errors.
 */
@Getter
public class BankException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    /**
     * Creates a BankException with an error code.
     *
     * @param errorCode the error code
     */
    public BankException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    /**
     * Creates a BankException with an error code and additional detail.
     *
     * @param errorCode the error code
     * @param detail additional detail about the error
     */
    public BankException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage() + ": " + detail);
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
