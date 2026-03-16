package az.hkbank.module.transaction.entity;

import az.hkbank.module.account.entity.Account;
import az.hkbank.module.account.entity.CurrencyType;
import az.hkbank.module.card.entity.Card;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction entity representing a financial transaction.
 * Supports P2P transfers, deposits, withdrawals, and utility payments.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(precision = 19, scale = 2)
    private BigDecimal convertedAmount;

    @Column(precision = 10, scale = 6)
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyType sourceCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyType targetCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_account_id", nullable = false)
    private Account senderAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_account_id", nullable = false)
    private Account receiverAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_card_id")
    private Card senderCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_card_id")
    private Card receiverCard;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (referenceNumber == null) {
            referenceNumber = generateReferenceNumber();
        }
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }

    /**
     * Generates a unique transaction reference number.
     * Format: "TXN" + first 8 characters of UUID (uppercase)
     *
     * @return generated reference number
     */
    private String generateReferenceNumber() {
        return "TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
