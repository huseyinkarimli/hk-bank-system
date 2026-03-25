package az.hkbank.module.account.service.impl;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.common.util.IbanGenerator;
import az.hkbank.module.account.dto.*;
import az.hkbank.module.account.entity.Account;
import az.hkbank.module.account.entity.AccountStatus;
import az.hkbank.module.account.entity.CurrencyType;
import az.hkbank.module.account.mapper.AccountMapper;
import az.hkbank.module.account.repository.AccountRepository;
import az.hkbank.module.account.service.AccountService;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of AccountService interface.
 * Handles account management operations with audit logging.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final String ACTION_ACCOUNT_CREATE = "ACCOUNT_CREATE";
    private static final String ACTION_ACCOUNT_STATUS_UPDATE = "ACCOUNT_STATUS_UPDATE";
    private static final String ACTION_ACCOUNT_DELETE = "ACCOUNT_DELETE";
    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;
    private final AuditService auditService;
    private final HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {
        log.info("Creating account for user: {}, currency: {}", userId, request.getCurrencyType());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BankException(ErrorCode.USER_NOT_FOUND));

        if (accountRepository.findByUserIdAndCurrencyType(userId, request.getCurrencyType()).isPresent()) {
            throw new BankException(ErrorCode.ACCOUNT_ALREADY_EXISTS,
                    "User already has an account with currency: " + request.getCurrencyType());
        }

        String accountNumber = generateUniqueAccountNumber(request.getCurrencyType());
        String iban = generateUniqueIban(request.getCurrencyType());

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .iban(iban)
                .balance(BigDecimal.ZERO)
                .currencyType(request.getCurrencyType())
                .status(AccountStatus.ACTIVE)
                .user(user)
                .isDeleted(false)
                .build();

        Account savedAccount = accountRepository.save(account);

        auditService.log(
                userId,
                ACTION_ACCOUNT_CREATE,
                "Account created: " + iban,
                getClientIpAddress()
        );

        log.info("Account created successfully: {}", iban);

        return accountMapper.toAccountResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long accountId, Long userId) {
        log.info("Fetching account: {} for user: {}", accountId, userId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.getUser().getId().equals(userId)) {
            throw new BankException(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS);
        }

        if (account.isDeleted()) {
            throw new BankException(ErrorCode.ACCOUNT_NOT_FOUND, "Account is deleted");
        }

        return accountMapper.toAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountSummaryResponse> getUserAccounts(Long userId) {
        log.info("Fetching all accounts for user: {}", userId);

        List<Account> accounts = accountRepository.findByUserIdAndIsDeletedFalse(userId);

        return accounts.stream()
                .map(accountMapper::toAccountSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByIban(String iban) {
        log.info("Fetching account by IBAN: {}", iban);

        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (account.isDeleted()) {
            throw new BankException(ErrorCode.ACCOUNT_NOT_FOUND, "Account is deleted");
        }

        return accountMapper.toAccountResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse updateAccountStatus(Long accountId, UpdateAccountStatusRequest request) {
        log.info("Updating account status: {} to {}", accountId, request.getStatus());

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (account.isDeleted()) {
            throw new BankException(ErrorCode.ACCOUNT_NOT_FOUND, "Account is deleted");
        }

        account.setStatus(request.getStatus());
        Account updatedAccount = accountRepository.save(account);

        auditService.log(
                account.getUser().getId(),
                ACTION_ACCOUNT_STATUS_UPDATE,
                "Account status updated to: " + request.getStatus() + " for IBAN: " + account.getIban(),
                getClientIpAddress()
        );

        log.info("Account status updated successfully: {}", accountId);

        return accountMapper.toAccountResponse(updatedAccount);
    }

    @Override
    @Transactional
    public void softDeleteAccount(Long accountId, Long userId) {
        log.info("Soft deleting account: {} for user: {}", accountId, userId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.getUser().getId().equals(userId)) {
            throw new BankException(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS);
        }

        if (account.isDeleted()) {
            throw new BankException(ErrorCode.ACCOUNT_NOT_FOUND, "Account is already deleted");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BankException(ErrorCode.VALIDATION_ERROR,
                    "Cannot delete account with positive balance");
        }

        account.setDeleted(true);
        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);

        auditService.log(
                userId,
                ACTION_ACCOUNT_DELETE,
                "Account soft deleted: " + account.getIban(),
                getClientIpAddress()
        );

        log.info("Account soft deleted successfully: {}", accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountSummaryResponse> getAllAccounts() {
        log.info("Fetching all active accounts");

        List<Account> accounts = accountRepository.findAllActive();

        return accounts.stream()
                .map(accountMapper::toAccountSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<CurrencyType, BigDecimal> getTotalBalanceByUser(Long userId) {
        log.info("Calculating total balance for user: {}", userId);

        List<Account> accounts = accountRepository.findByUserIdAndIsDeletedFalse(userId);

        return accounts.stream()
                .filter(account -> account.getStatus() == AccountStatus.ACTIVE)
                .collect(Collectors.groupingBy(
                        Account::getCurrencyType,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Account::getBalance,
                                BigDecimal::add
                        )
                ));
    }

    private String generateUniqueAccountNumber(CurrencyType currencyType) {
        for (int i = 0; i < MAX_GENERATION_ATTEMPTS; i++) {
            String accountNumber = IbanGenerator.generateAccountNumber(currencyType);
            if (!accountRepository.existsByAccountNumber(accountNumber)) {
                return accountNumber;
            }
        }
        throw new BankException(ErrorCode.INTERNAL_SERVER_ERROR,
                "Failed to generate unique account number");
    }

    private String generateUniqueIban(CurrencyType currencyType) {
        for (int i = 0; i < MAX_GENERATION_ATTEMPTS; i++) {
            String iban = IbanGenerator.generateIban(currencyType);
            if (!accountRepository.existsByIban(iban)) {
                return iban;
            }
        }
        throw new BankException(ErrorCode.INTERNAL_SERVER_ERROR,
                "Failed to generate unique IBAN");
    }

    private String getClientIpAddress() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return httpServletRequest.getRemoteAddr();
    }
}
