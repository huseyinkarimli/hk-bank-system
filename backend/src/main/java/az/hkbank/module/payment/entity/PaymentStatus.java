package az.hkbank.module.payment.entity;

/**
 * Enumeration of payment statuses in the payment lifecycle.
 * Tracks the current state of a utility payment transaction.
 */
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REJECTED
}
