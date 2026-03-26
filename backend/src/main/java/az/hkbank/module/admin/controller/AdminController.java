package az.hkbank.module.admin.controller;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.common.response.ApiResponse;
import az.hkbank.module.account.dto.AccountResponse;
import az.hkbank.module.account.entity.Account;
import az.hkbank.module.account.mapper.AccountMapper;
import az.hkbank.module.account.repository.AccountRepository;
import az.hkbank.module.admin.dto.*;
import az.hkbank.module.admin.service.AdminDashboardService;
import az.hkbank.module.admin.service.AdminUserBanService;
import az.hkbank.module.audit.dto.AuditLogResponse;
import az.hkbank.module.audit.entity.AuditLog;
import az.hkbank.module.audit.mapper.AuditLogMapper;
import az.hkbank.module.audit.repository.AuditLogRepository;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.card.dto.CardSummaryResponse;
import az.hkbank.module.card.entity.Card;
import az.hkbank.module.card.entity.CardStatus;
import az.hkbank.module.card.mapper.CardMapper;
import az.hkbank.module.card.repository.CardRepository;
import az.hkbank.module.transaction.dto.TransactionSummaryResponse;
import az.hkbank.module.transaction.entity.Transaction;
import az.hkbank.module.transaction.entity.TransactionStatus;
import az.hkbank.module.transaction.mapper.TransactionMapper;
import az.hkbank.module.transaction.repository.TransactionRepository;
import az.hkbank.module.transaction.service.TransactionService;
import az.hkbank.module.user.dto.UserResponse;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.user.mapper.UserMapper;
import az.hkbank.module.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for admin operations.
 * Provides comprehensive management endpoints for users, accounts, transactions, and cards.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Management", description = "Administrative endpoints for system management")
public class AdminController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;
    private final CardMapper cardMapper;
    private final AuditLogMapper auditLogMapper;
    private final AuditService auditService;
    private final TransactionService transactionService;
    private final AdminUserBanService adminUserBanService;
    private final AdminDashboardService adminDashboardService;
    private final HttpServletRequest httpServletRequest;

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieves all users with pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access forbidden - Admin role required"
            )
    })
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(Pageable pageable) {
        log.info("Admin fetching all users");

        Page<UserResponse> users = userRepository.findAll(pageable)
                .map(userMapper::toUserResponse);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves detailed user information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.info("Admin fetching user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BankException(ErrorCode.USER_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success(userMapper.toUserResponse(user)));
    }

    @PutMapping("/users/{userId}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ban user", description = "Soft-deletes user, blocks all cards, rejects JWT, notifies user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User banned",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    public ResponseEntity<ApiResponse<String>> banUser(
            @PathVariable Long userId,
            @Valid @RequestBody BanUserRequest request) {
        log.info("Admin banning user: {}", userId);
        adminUserBanService.banUser(userId, request.getReason(), getClientIpAddress());
        return ResponseEntity.ok(ApiResponse.success("İstifadəçi uğurla banlandı"));
    }

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dashboard statistics", description = "Aggregated users, cards, transactions, and total AZN balance")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Statistics retrieved",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> getDashboardStats() {
        log.info("Admin fetching dashboard statistics");
        return ResponseEntity.ok(ApiResponse.success(adminDashboardService.getDashboardStats()));
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Change user role", description = "Updates a user's role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Role changed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    public ResponseEntity<ApiResponse<UserResponse>> changeUserRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeUserRoleRequest request) {
        log.info("Admin changing role for user: {} to {}", id, request.getRole());

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BankException(ErrorCode.USER_NOT_FOUND));

        String oldRole = user.getRole().toString();
        user.setRole(request.getRole());
        User updatedUser = userRepository.save(user);

        auditService.log(
                user.getId(),
                "USER_ROLE_CHANGED",
                "User role changed from " + oldRole + " to " + request.getRole(),
                getClientIpAddress(),
                "USER",
                user.getId(),
                oldRole,
                request.getRole().toString()
        );

        log.info("User role changed successfully: {}", id);

        return ResponseEntity.ok(ApiResponse.success(userMapper.toUserResponse(updatedUser)));
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "Change user status", description = "Activates or deactivates a user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Status changed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    public ResponseEntity<ApiResponse<UserResponse>> changeUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeUserStatusRequest request) {
        log.info("Admin changing status for user: {} to active={}", id, request.getActive());

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BankException(ErrorCode.USER_NOT_FOUND));

        boolean oldStatus = !user.isDeleted();
        user.setDeleted(!request.getActive());
        User updatedUser = userRepository.save(user);

        auditService.log(
                user.getId(),
                "USER_STATUS_CHANGED",
                "User status changed to " + (request.getActive() ? "ACTIVE" : "INACTIVE"),
                getClientIpAddress(),
                "USER",
                user.getId(),
                String.valueOf(oldStatus),
                String.valueOf(request.getActive())
        );

        log.info("User status changed successfully: {}", id);

        return ResponseEntity.ok(ApiResponse.success(userMapper.toUserResponse(updatedUser)));
    }

    @GetMapping("/accounts")
    @Operation(summary = "Get all accounts", description = "Retrieves all accounts with pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Accounts retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<Page<AccountResponse>>> getAllAccounts(Pageable pageable) {
        log.info("Admin fetching all accounts");

        Page<AccountResponse> accounts = accountRepository.findAll(pageable)
                .map(accountMapper::toAccountResponse);

        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/accounts/{id}")
    @Operation(summary = "Get account by ID", description = "Retrieves detailed account information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Account retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable Long id) {
        log.info("Admin fetching account: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success(accountMapper.toAccountResponse(account)));
    }

    @PostMapping("/accounts/{accountId}/deposit")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin deposit", description = "Credits an account balance and records a DEPOSIT transaction")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Deposit completed",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    public ResponseEntity<ApiResponse<AccountResponse>> adminDeposit(
            @PathVariable Long accountId,
            @Valid @RequestBody AdminFundRequest request) {
        log.info("Admin deposit to account: {}", accountId);

        AccountResponse updated = transactionService.adminDeposit(accountId, request, getClientIpAddress());
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin withdrawal", description = "Debits an account balance and records a WITHDRAWAL transaction")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Withdrawal completed",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    public ResponseEntity<ApiResponse<AccountResponse>> adminWithdraw(
            @PathVariable Long accountId,
            @Valid @RequestBody AdminFundRequest request) {
        log.info("Admin withdrawal from account: {}", accountId);

        AccountResponse updated = transactionService.adminWithdraw(accountId, request, getClientIpAddress());
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PutMapping("/accounts/{id}/status")
    @Operation(summary = "Change account status", description = "Updates an account's status")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Status changed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    public ResponseEntity<ApiResponse<AccountResponse>> changeAccountStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeAccountStatusRequest request) {
        log.info("Admin changing status for account: {} to {}", id, request.getStatus());

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

        String oldStatus = account.getStatus().toString();
        account.setStatus(request.getStatus());
        Account updatedAccount = accountRepository.save(account);

        String description = "Account status changed from " + oldStatus + " to " + request.getStatus();
        if (request.getReason() != null) {
            description += ". Reason: " + request.getReason();
        }

        auditService.log(
                account.getUser().getId(),
                "ACCOUNT_STATUS_CHANGED",
                description,
                getClientIpAddress(),
                "ACCOUNT",
                account.getId(),
                oldStatus,
                request.getStatus().toString()
        );

        log.info("Account status changed successfully: {}", id);

        return ResponseEntity.ok(ApiResponse.success(accountMapper.toAccountResponse(updatedAccount)));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get all transactions", description = "Retrieves all transactions with pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transactions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<Page<TransactionSummaryResponse>>> getAllTransactions(Pageable pageable) {
        log.info("Admin fetching all transactions");

        Page<TransactionSummaryResponse> transactions = transactionRepository.findAll(pageable)
                .map(transactionMapper::toTransactionSummaryResponse);

        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/transactions/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves detailed transaction information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transaction retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found"
            )
    })
    public ResponseEntity<ApiResponse<TransactionSummaryResponse>> getTransactionById(@PathVariable Long id) {
        log.info("Admin fetching transaction: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new BankException(ErrorCode.TRANSACTION_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success(transactionMapper.toTransactionSummaryResponse(transaction)));
    }

    @GetMapping("/transactions/stats")
    @Operation(summary = "Get transaction statistics", description = "Retrieves aggregated transaction statistics")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<TransactionStatsResponse>> getTransactionStats() {
        log.info("Admin fetching transaction statistics");

        List<Transaction> allTransactions = transactionRepository.findAll();

        long totalCount = allTransactions.size();
        BigDecimal totalVolume = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> countByStatus = allTransactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getStatus().toString(),
                        Collectors.counting()
                ));

        Map<String, BigDecimal> dailyVolume = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().toLocalDate().toString(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));

        TransactionStatsResponse stats = TransactionStatsResponse.builder()
                .totalTransactions(totalCount)
                .totalVolume(totalVolume)
                .countByStatus(countByStatus)
                .dailyVolume(dailyVolume)
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/cards")
    @Operation(summary = "Get all cards", description = "Retrieves all cards with pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cards retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<Page<CardSummaryResponse>>> getAllCards(Pageable pageable) {
        log.info("Admin fetching all cards");

        Page<CardSummaryResponse> cards = cardRepository.findAll(pageable)
                .map(cardMapper::toCardSummaryResponse);

        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    @PutMapping("/cards/{id}/status")
    @Operation(summary = "Change card status", description = "Blocks or unblocks a card")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Card status changed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Card not found"
            )
    })
    public ResponseEntity<ApiResponse<CardSummaryResponse>> changeCardStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeAccountStatusRequest request) {
        log.info("Admin changing card status: {} to {}", id, request.getStatus());

        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new BankException(ErrorCode.CARD_NOT_FOUND));

        CardStatus oldStatus = card.getStatus();
        CardStatus newStatus = request.getStatus().toString().equals("BLOCKED") 
                ? CardStatus.BLOCKED : CardStatus.ACTIVE;
        
        card.setStatus(newStatus);
        Card updatedCard = cardRepository.save(card);

        auditService.log(
                card.getAccount().getUser().getId(),
                "CARD_STATUS_CHANGED_BY_ADMIN",
                "Card status changed from " + oldStatus + " to " + newStatus + 
                        (request.getReason() != null ? ". Reason: " + request.getReason() : ""),
                getClientIpAddress(),
                "CARD",
                card.getId(),
                oldStatus.toString(),
                newStatus.toString()
        );

        log.info("Card status changed successfully: {}", id);

        return ResponseEntity.ok(ApiResponse.success(cardMapper.toCardSummaryResponse(updatedCard)));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get all audit logs", description = "Retrieves all audit logs with pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit logs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAllAuditLogs(Pageable pageable) {
        log.info("Admin fetching all audit logs");

        Page<AuditLogResponse> auditLogs = auditLogRepository.findAll(pageable)
                .map(auditLogMapper::toAuditLogResponse);

        return ResponseEntity.ok(ApiResponse.success(auditLogs));
    }

    @GetMapping("/audit-logs/user/{userId}")
    @Operation(summary = "Get audit logs by user", description = "Retrieves audit logs for a specific user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit logs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByUser(@PathVariable Long userId) {
        log.info("Admin fetching audit logs for user: {}", userId);

        List<AuditLogResponse> auditLogs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(auditLogMapper::toAuditLogResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(auditLogs));
    }

    @GetMapping("/audit-logs/actions")
    @Operation(summary = "Get audit logs by action", description = "Retrieves audit logs filtered by action type")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit logs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByAction(
            @RequestParam String action) {
        log.info("Admin fetching audit logs for action: {}", action);

        List<AuditLogResponse> auditLogs = auditLogRepository.findByAction(action)
                .stream()
                .map(auditLogMapper::toAuditLogResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(auditLogs));
    }

    private String getClientIpAddress() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return httpServletRequest.getRemoteAddr();
    }
}
