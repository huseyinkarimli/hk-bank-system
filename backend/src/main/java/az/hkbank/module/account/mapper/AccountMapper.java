package az.hkbank.module.account.mapper;

import az.hkbank.module.account.dto.AccountResponse;
import az.hkbank.module.account.dto.AccountSummaryResponse;
import az.hkbank.module.account.entity.Account;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for Account entity conversions.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * Converts Account entity to AccountResponse DTO.
     *
     * @param account the account entity
     * @return AccountResponse DTO
     */
    AccountResponse toAccountResponse(Account account);

    /**
     * Converts Account entity to AccountSummaryResponse DTO.
     *
     * @param account the account entity
     * @return AccountSummaryResponse DTO
     */
    AccountSummaryResponse toAccountSummaryResponse(Account account);
}
