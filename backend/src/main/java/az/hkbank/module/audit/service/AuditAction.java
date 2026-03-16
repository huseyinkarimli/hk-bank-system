package az.hkbank.module.audit.service;

/**
 * Constants for audit log actions.
 * Defines standardized action names for audit logging.
 */
public final class AuditAction {

    public static final String LOGIN = "LOGIN";
    public static final String REGISTER = "REGISTER";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";
    
    public static final String PAYMENT_SUCCESS = "PAYMENT_SUCCESS";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";

    private AuditAction() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
