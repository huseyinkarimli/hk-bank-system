package az.hkbank.module.payment.controller;

import az.hkbank.common.response.ApiResponse;
import az.hkbank.module.payment.dto.PaymentRequest;
import az.hkbank.module.payment.dto.PaymentResponse;
import az.hkbank.module.payment.dto.PaymentSummaryResponse;
import az.hkbank.module.payment.dto.ProviderListResponse;
import az.hkbank.module.payment.service.PaymentService;
import az.hkbank.module.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for utility payment operations.
 * Handles payment processing, payment history, and provider management endpoints.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Payment Management", description = "Utility payment processing and history endpoints")
public class PaymentController {

    private final PaymentService paymentService;
    private final HttpServletRequest httpServletRequest;

    /**
     * Processes a utility payment.
     *
     * @param currentUser the authenticated user
     * @param request the payment request
     * @return ApiResponse containing payment information
     */
    @PostMapping
    @Operation(summary = "Make utility payment", description = "Processes a payment to a utility provider")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Payment completed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error, insufficient balance, payment limit exceeded, or provider rejected"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Account blocked or unauthorized access"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Payment failed"
            )
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> makePayment(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PaymentRequest request) {
        String ipAddress = getClientIpAddress();
        PaymentResponse response = paymentService.makePayment(currentUser.getId(), request, ipAddress);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment completed successfully", response));
    }

    /**
     * Retrieves user payment history.
     *
     * @param currentUser the authenticated user
     * @return ApiResponse containing list of payments
     */
    @GetMapping
    @Operation(summary = "Get user payments", description = "Retrieves payment history for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<ApiResponse<List<PaymentSummaryResponse>>> getUserPayments(
            @AuthenticationPrincipal User currentUser) {
        List<PaymentSummaryResponse> payments = paymentService.getUserPayments(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    /**
     * Retrieves a specific payment by ID.
     *
     * @param id the payment ID
     * @param currentUser the authenticated user
     * @return ApiResponse containing payment information
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves a specific payment by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access to payment"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Payment not found"
            )
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        PaymentResponse response = paymentService.getPaymentById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves a payment by reference number.
     *
     * @param referenceNumber the payment reference number
     * @return ApiResponse containing payment information
     */
    @GetMapping("/reference/{referenceNumber}")
    @Operation(summary = "Get payment by reference", description = "Retrieves a payment by reference number")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Payment not found"
            )
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByReference(
            @PathVariable String referenceNumber) {
        PaymentResponse response = paymentService.getPaymentByReference(referenceNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves payments for a specific account.
     *
     * @param accountId the account ID
     * @param currentUser the authenticated user
     * @return ApiResponse containing list of payments
     */
    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get payments by account", description = "Retrieves all payments for a specific account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access to account"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    public ResponseEntity<ApiResponse<List<PaymentSummaryResponse>>> getPaymentsByAccount(
            @PathVariable Long accountId,
            @AuthenticationPrincipal User currentUser) {
        List<PaymentSummaryResponse> payments = paymentService.getPaymentsByAccount(accountId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    /**
     * Retrieves list of available utility providers.
     *
     * @return ApiResponse containing list of providers
     */
    @GetMapping("/providers")
    @Operation(summary = "Get available providers", description = "Retrieves list of all available utility providers")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Providers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<ApiResponse<List<ProviderListResponse>>> getAvailableProviders() {
        List<ProviderListResponse> providers = paymentService.getAvailableProviders();
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    /**
     * Retrieves all payments (admin only).
     *
     * @param pageable pagination parameters
     * @return ApiResponse containing page of payments
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all payments", description = "Retrieves all payments (admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin access required"
            )
    })
    public ResponseEntity<ApiResponse<Page<PaymentSummaryResponse>>> getAllPayments(
            Pageable pageable) {
        Page<PaymentSummaryResponse> payments = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    /**
     * Extracts client IP address from request.
     *
     * @return client IP address
     */
    private String getClientIpAddress() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return httpServletRequest.getRemoteAddr();
    }
}
