package az.hkbank.module.payment.service;

import az.hkbank.module.payment.dto.PaymentRequest;
import az.hkbank.module.payment.dto.PaymentResponse;
import az.hkbank.module.payment.dto.PaymentSummaryResponse;
import az.hkbank.module.payment.dto.ProviderListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for utility payment operations.
 * Defines methods for processing payments, retrieving payment history, and managing providers.
 */
public interface PaymentService {

    /**
     * Processes a utility payment to a provider.
     *
     * @param userId the user ID making the payment
     * @param request the payment request details
     * @param ipAddress the IP address of the request
     * @return payment response with transaction details
     */
    PaymentResponse makePayment(Long userId, PaymentRequest request, String ipAddress);

    /**
     * Retrieves a payment by ID.
     *
     * @param id the payment ID
     * @param userId the user ID requesting the payment
     * @return payment response
     */
    PaymentResponse getPaymentById(Long id, Long userId);

    /**
     * Retrieves all payments for a user.
     *
     * @param userId the user ID
     * @return list of payment summaries
     */
    List<PaymentSummaryResponse> getUserPayments(Long userId);

    /**
     * Retrieves all payments for a specific account.
     *
     * @param accountId the account ID
     * @param userId the user ID requesting the payments
     * @return list of payment summaries
     */
    List<PaymentSummaryResponse> getPaymentsByAccount(Long accountId, Long userId);

    /**
     * Retrieves a payment by reference number.
     *
     * @param referenceNumber the payment reference number
     * @return payment response
     */
    PaymentResponse getPaymentByReference(String referenceNumber);

    /**
     * Gets list of all available utility providers.
     *
     * @return list of providers grouped by type
     */
    List<ProviderListResponse> getAvailableProviders();

    /**
     * Retrieves all payments (admin operation).
     *
     * @param pageable pagination parameters
     * @return page of payment summaries
     */
    Page<PaymentSummaryResponse> getAllPayments(Pageable pageable);
}
