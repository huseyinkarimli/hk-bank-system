package az.hkbank.module.account.dto;

import az.hkbank.module.account.entity.AccountStatus;
import az.hkbank.module.account.entity.CurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for lightweight account summary responses.
 * Used in list operations to reduce data transfer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSummaryResponse {

    private Long id;
    private String accountNumber;
    private String iban;
    private BigDecimal balance;
    private CurrencyType currencyType;
    private AccountStatus status;
}
