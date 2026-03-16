package az.hkbank.module.transaction.controller;

import az.hkbank.common.response.ApiResponse;
import az.hkbank.module.transaction.dto.*;
import az.hkbank.module.transaction.service.TransactionService;
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

/**
 * REST controller for transaction operations.
 * Handles money transfers and transaction history endpoints.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transaction Management", description = "Money transfer and transaction history endpoints")
public class TransactionController {

    private final TransactionService transactionService;
    private final HttpServletRequest httpServletRequest;

    /**
     * Transfers money between cards.
     *
     * @param currentUser the authenticated user
     * @param request the card transfer request
     * @return ApiResponse containing transaction information
     */
    @PostMapping("/transfer/card")
    @Operation(summary = "P2P card transfer", description = "Transfers money between two cards")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Transfer completed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error, insufficient balance, or limit exceeded"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Card blocked/frozen, fraud detected, or unauthorized access"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Card not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Transaction failed"
            )
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> transferByCard(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody P2PCardTransferRequest request) {
        String ipAddress = getClientIpAddress();
        TransactionResponse response = transactionService.transferByCard(
                currentUser.getId(), request, ipAddress);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer completed successfully", response));
    }

    /**
     * Transfers money between accounts using IBAN.
     *
     * @param currentUser the authenticated user
     * @param request the IBAN transfer request
     * @return ApiResponse containing transaction information
     */
    @PostMapping("/transfer/iban")
    @Operation(summary = "P2P IBAN transfer", description = "Transfers money between two accounts using IBAN")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Transfer completed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error, insufficient balance, or limit exceeded"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Account blocked/closed or unauthorized access"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Transaction failed"
            )
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> transferByIban(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody P2PIbanTransferRequest request) {
        String ipAddress = getClientIpAddress();
        TransactionResponse response = transactionService.transferByIban(
                currentUser.getId(), request, ipAddress);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer completed successfully", response));
    }

    /**
     * Retrieves user transaction history with optional filters.
     *
     * @param currentUser the authenticated user
     * @param filter the filter criteria
     * @param pageable pagination parameters
     * @return ApiResponse containing page of transactions
     */
    @GetMapping
    @Operation(summary = "Get user transactions", description = "Retrieves transaction history for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transactions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<ApiResponse<Page<TransactionSummaryResponse>>> getUserTransactions(
            @AuthenticationPrincipal User currentUser,
            @ModelAttribute TransactionFilterRequest filter,
            Pageable pageable) {
        Page<TransactionSummaryResponse> transactions = transactionService.getUserTransactions(
                currentUser.getId(), filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    /**
     * Retrieves a specific transaction by ID.
     *
     * @param id the transaction ID
     * @param currentUser the authenticated user
     * @return ApiResponse containing transaction information
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves a specific transaction by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transaction retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access to transaction"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found"
            )
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        TransactionResponse response = transactionService.getTransactionById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves a transaction by reference number.
     *
     * @param referenceNumber the reference number
     * @return ApiResponse containing transaction information
     */
    @GetMapping("/reference/{referenceNumber}")
    @Operation(summary = "Get transaction by reference", description = "Retrieves a transaction by reference number")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transaction retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found"
            )
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByReference(
            @PathVariable String referenceNumber) {
        TransactionResponse response = transactionService.getTransactionByReference(referenceNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves all transactions (admin only).
     *
     * @param pageable pagination parameters
     * @return ApiResponse containing page of transactions
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all transactions", description = "Retrieves all transactions (admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transactions retrieved successfully",
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
    public ResponseEntity<ApiResponse<Page<TransactionSummaryResponse>>> getAllTransactions(
            Pageable pageable) {
        Page<TransactionSummaryResponse> transactions = transactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
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
