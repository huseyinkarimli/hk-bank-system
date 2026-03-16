package az.hkbank.module.payment.service.impl;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.account.entity.Account;
import az.hkbank.module.account.entity.AccountStatus;
import az.hkbank.module.account.repository.AccountRepository;
import az.hkbank.module.audit.service.AuditAction;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.notification.entity.NotificationType;
import az.hkbank.module.notification.service.NotificationService;
import az.hkbank.module.payment.dto.PaymentRequest;
import az.hkbank.module.payment.dto.PaymentResponse;
import az.hkbank.module.payment.dto.PaymentSummaryResponse;
import az.hkbank.module.payment.dto.ProviderListResponse;
import az.hkbank.module.payment.entity.Payment;
import az.hkbank.module.payment.entity.PaymentStatus;
import az.hkbank.module.payment.entity.ProviderType;
import az.hkbank.module.payment.mapper.PaymentMapper;
import az.hkbank.module.payment.repository.PaymentRepository;
import az.hkbank.module.payment.service.PaymentService;
import az.hkbank.module.payment.service.ProviderSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of PaymentService interface.
 * Handles utility payment processing with provider simulation, balance management, and audit logging.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final BigDecimal MAX_PAYMENT_AMOUNT = new BigDecimal("500.00");

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final PaymentMapper paymentMapper;
    private final ProviderSimulationService providerSimulationService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PaymentResponse makePayment(Long userId, PaymentRequest request, String ipAddress) {
        log.info("Processing payment for user: {}, provider: {}, amount: {}",
                userId, request.getProviderName(), request.getAmount());

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.getUser().getId().equals(userId)) {
            throw new BankException(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS, "Account does not belong to user");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BankException(ErrorCode.ACCOUNT_BLOCKED, "Account is not active");
        }

        if (request.getAmount().compareTo(MAX_PAYMENT_AMOUNT) > 0) {
            throw new BankException(ErrorCode.PAYMENT_LIMIT_EXCEEDED, 
                    "Payment amount exceeds maximum limit of " + MAX_PAYMENT_AMOUNT + " AZN");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BankException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        Payment payment = Payment.builder()
                .providerType(request.getProviderType())
                .providerName(request.getProviderName())
                .subscriberNumber(request.getSubscriberNumber())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .account(account)
                .description(request.getDescription())
                .ipAddress(ipAddress)
                .build();

        try {
            Account lockedAccount = accountRepository.findByIdForUpdate(account.getId())
                    .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

            if (lockedAccount.getBalance().compareTo(request.getAmount()) < 0) {
                payment.setStatus(PaymentStatus.REJECTED);
                payment.setFailureReason("Insufficient balance");
                paymentRepository.save(payment);

                auditService.log(userId, AuditAction.PAYMENT_FAILED,
                        "Payment rejected: Insufficient balance - " + payment.getReferenceNumber(),
                        ipAddress);

                throw new BankException(ErrorCode.INSUFFICIENT_BALANCE);
            }

            lockedAccount.setBalance(lockedAccount.getBalance().subtract(request.getAmount()));
            accountRepository.save(lockedAccount);

            boolean providerSuccess = providerSimulationService.simulatePayment(
                    request.getProviderType(),
                    request.getSubscriberNumber(),
                    request.getAmount()
            );

            if (providerSuccess) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setCompletedAt(LocalDateTime.now());

                Payment savedPayment = paymentRepository.save(payment);

                auditService.log(userId, AuditAction.PAYMENT_SUCCESS,
                        "Payment completed: " + request.getAmount() + " AZN to " +
                                request.getProviderName() + " - " + savedPayment.getReferenceNumber(),
                        ipAddress);

                notificationService.createNotification(
                        userId,
                        NotificationType.PAYMENT,
                        "Ödəniş uğurla tamamlandı",
                        "Ödəniş uğurla tamamlandı: " + request.getProviderName() + " - " + request.getAmount() + " AZN"
                );

                log.info("Payment completed successfully: {}", savedPayment.getReferenceNumber());

                return paymentMapper.toPaymentResponse(savedPayment);

            } else {
                lockedAccount.setBalance(lockedAccount.getBalance().add(request.getAmount()));
                accountRepository.save(lockedAccount);

                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Payment rejected by provider");

                Payment savedPayment = paymentRepository.save(payment);

                auditService.log(userId, AuditAction.PAYMENT_FAILED,
                        "Payment rejected by provider: " + request.getProviderName() + " - " + savedPayment.getReferenceNumber(),
                        ipAddress);

                log.warn("Payment rejected by provider: {}", savedPayment.getReferenceNumber());

                throw new BankException(ErrorCode.PROVIDER_REJECTED, "Provider rejected the payment");
            }

        } catch (BankException e) {
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error during payment processing", e);

            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Internal error: " + e.getMessage());
            paymentRepository.save(payment);

            auditService.log(userId, AuditAction.PAYMENT_FAILED,
                    "Payment failed: Internal error - " + payment.getReferenceNumber(),
                    ipAddress);

            throw new BankException(ErrorCode.PAYMENT_FAILED, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id, Long userId) {
        log.info("Fetching payment: {} for user: {}", id, userId);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new BankException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getAccount().getUser().getId().equals(userId)) {
            throw new BankException(ErrorCode.FORBIDDEN, "Unauthorized access to payment");
        }

        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentSummaryResponse> getUserPayments(Long userId) {
        log.info("Fetching all payments for user: {}", userId);

        List<Payment> payments = paymentRepository.findByAccountUserId(userId);

        return payments.stream()
                .map(paymentMapper::toPaymentSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentSummaryResponse> getPaymentsByAccount(Long accountId, Long userId) {
        log.info("Fetching payments for account: {}, user: {}", accountId, userId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.getUser().getId().equals(userId)) {
            throw new BankException(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS, "Account does not belong to user");
        }

        List<Payment> payments = paymentRepository.findByAccountId(accountId);

        return payments.stream()
                .map(paymentMapper::toPaymentSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReference(String referenceNumber) {
        log.info("Fetching payment by reference: {}", referenceNumber);

        Payment payment = paymentRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new BankException(ErrorCode.PAYMENT_NOT_FOUND));

        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderListResponse> getAvailableProviders() {
        log.info("Fetching available providers");

        Map<ProviderType, List<String>> allProviders = providerSimulationService.getAllProviders();

        return allProviders.entrySet().stream()
                .map(entry -> ProviderListResponse.builder()
                        .providerType(entry.getKey())
                        .providers(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentSummaryResponse> getAllPayments(Pageable pageable) {
        log.info("Fetching all payments (admin)");

        Page<Payment> payments = paymentRepository.findAll(pageable);

        return payments.map(paymentMapper::toPaymentSummaryResponse);
    }
}
