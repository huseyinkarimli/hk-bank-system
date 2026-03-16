package az.hkbank.module.payment.mapper;

import az.hkbank.module.payment.dto.PaymentResponse;
import az.hkbank.module.payment.dto.PaymentSummaryResponse;
import az.hkbank.module.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Payment entity transformations.
 * Converts between Payment entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

    /**
     * Converts Payment entity to PaymentResponse DTO.
     * Maps account number with masking for security.
     *
     * @param payment the payment entity
     * @return payment response DTO
     */
    @Mapping(target = "accountNumber", expression = "java(maskAccountNumber(payment.getAccount().getAccountNumber()))")
    PaymentResponse toPaymentResponse(Payment payment);

    /**
     * Converts Payment entity to PaymentSummaryResponse DTO.
     *
     * @param payment the payment entity
     * @return payment summary response DTO
     */
    PaymentSummaryResponse toPaymentSummaryResponse(Payment payment);

    /**
     * Masks account number for security (shows last 4 digits only).
     *
     * @param accountNumber the full account number
     * @return masked account number
     */
    default String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "******" + accountNumber.substring(accountNumber.length() - 4);
    }
}
