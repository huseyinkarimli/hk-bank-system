package az.hkbank.module.payment.dto;

import az.hkbank.module.payment.entity.PaymentStatus;
import az.hkbank.module.payment.entity.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment details.
 * Contains complete information about a payment transaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private String referenceNumber;
    private ProviderType providerType;
    private String providerName;
    private String subscriberNumber;
    private BigDecimal amount;
    private PaymentStatus status;
    private String description;
    private String failureReason;
    private String accountNumber;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
