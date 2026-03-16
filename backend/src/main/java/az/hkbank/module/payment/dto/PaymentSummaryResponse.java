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
 * Summary response DTO for payment listings.
 * Contains essential payment information for list views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummaryResponse {

    private Long id;
    private String referenceNumber;
    private ProviderType providerType;
    private String providerName;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;
}
