package az.hkbank.module.transaction.mapper;

import az.hkbank.module.transaction.dto.TransactionResponse;
import az.hkbank.module.transaction.dto.TransactionSummaryResponse;
import az.hkbank.module.transaction.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Transaction entity conversions.
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    /**
     * Converts Transaction entity to TransactionResponse DTO.
     *
     * @param transaction the transaction entity
     * @return TransactionResponse DTO
     */
    @Mapping(target = "senderAccountNumber", source = "senderAccount.accountNumber")
    @Mapping(target = "receiverAccountNumber", source = "receiverAccount.accountNumber")
    TransactionResponse toTransactionResponse(Transaction transaction);

    /**
     * Converts Transaction entity to TransactionSummaryResponse DTO.
     *
     * @param transaction the transaction entity
     * @return TransactionSummaryResponse DTO
     */
    TransactionSummaryResponse toTransactionSummaryResponse(Transaction transaction);
}
