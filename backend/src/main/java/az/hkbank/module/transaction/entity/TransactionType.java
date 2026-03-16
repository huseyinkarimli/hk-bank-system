package az.hkbank.module.transaction.entity;

/**
 * Transaction type enumeration for the HK Bank System.
 * Defines the types of financial transactions supported.
 */
public enum TransactionType {
    P2P_CARD,
    P2P_IBAN,
    DEPOSIT,
    WITHDRAWAL,
    UTILITY_PAYMENT
}
