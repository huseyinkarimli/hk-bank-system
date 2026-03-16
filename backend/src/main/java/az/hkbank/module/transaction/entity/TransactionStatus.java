package az.hkbank.module.transaction.entity;

/**
 * Transaction status enumeration for the HK Bank System.
 * Tracks the lifecycle state of a transaction.
 */
public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REJECTED
}
