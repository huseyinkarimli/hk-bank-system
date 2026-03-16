package az.hkbank.module.account.controller;

import az.hkbank.common.response.ApiResponse;
import az.hkbank.module.account.dto.*;
import az.hkbank.module.account.entity.CurrencyType;
import az.hkbank.module.account.service.AccountService;
import az.hkbank.module.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for account management operations.
 * Handles account creation, retrieval, and management endpoints.
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Account Management", description = "Account creation and management endpoints")
public class AccountController {

    private final AccountService accountService;

    /**
     * Creates a new account for the authenticated user.
     *
     * @param currentUser the authenticated user
     * @param request the account creation request
     * @return ApiResponse containing created account information
     */
    @PostMapping
    @Operation(summary = "Create new account", description = "Creates a new bank account for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Account already exists for this currency"
            )
    })
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse accountResponse = accountService.createAccount(currentUser.getId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", accountResponse));
    }

    /**
     * Retrieves all accounts for the authenticated user.
     *
     * @param currentUser the authenticated user
     * @return ApiResponse containing list of user accounts
     */
    @GetMapping
    @Operation(summary = "Get user accounts", description = "Retrieves all accounts for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Accounts retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<ApiResponse<List<AccountSummaryResponse>>> getUserAccounts(
            @AuthenticationPrincipal User currentUser) {
        List<AccountSummaryResponse> accounts = accountService.getUserAccounts(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    /**
     * Retrieves a specific account by ID.
     *
     * @param id the account ID
     * @param currentUser the authenticated user
     * @return ApiResponse containing account information
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID", description = "Retrieves a specific account by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Account retrieved successfully",
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
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        AccountResponse accountResponse = accountService.getAccountById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(accountResponse));
    }

    /**
     * Retrieves an account by IBAN.
     *
     * @param iban the IBAN
     * @return ApiResponse containing account information
     */
    @GetMapping("/iban/{iban}")
    @Operation(summary = "Get account by IBAN", description = "Retrieves an account by IBAN")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Account retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByIban(@PathVariable String iban) {
        AccountResponse accountResponse = accountService.getAccountByIban(iban);
        return ResponseEntity.ok(ApiResponse.success(accountResponse));
    }

    /**
     * Retrieves total balance summary per currency for the authenticated user.
     *
     * @param currentUser the authenticated user
     * @return ApiResponse containing balance summary
     */
    @GetMapping("/balance-summary")
    @Operation(summary = "Get balance summary", description = "Retrieves total balance per currency for the user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Balance summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<ApiResponse<Map<CurrencyType, BigDecimal>>> getBalanceSummary(
            @AuthenticationPrincipal User currentUser) {
        Map<CurrencyType, BigDecimal> balanceSummary = accountService.getTotalBalanceByUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(balanceSummary));
    }

    /**
     * Soft deletes an account.
     *
     * @param id the account ID
     * @param currentUser the authenticated user
     * @return ApiResponse with success message
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete account", description = "Soft deletes an account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Account deleted successfully"
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
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        accountService.softDeleteAccount(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }

    /**
     * Retrieves all accounts (admin only).
     *
     * @return ApiResponse containing list of all accounts
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all accounts", description = "Retrieves all active accounts (admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Accounts retrieved successfully",
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
    public ResponseEntity<ApiResponse<List<AccountSummaryResponse>>> getAllAccounts() {
        List<AccountSummaryResponse> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    /**
     * Updates account status (admin only).
     *
     * @param id the account ID
     * @param request the status update request
     * @return ApiResponse containing updated account information
     */
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update account status", description = "Updates account status (admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Account status updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin access required"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccountStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountStatusRequest request) {
        AccountResponse accountResponse = accountService.updateAccountStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Account status updated successfully", accountResponse));
    }
}
