package az.hkbank.module.card.mapper;

import az.hkbank.common.util.CardNumberGenerator;
import az.hkbank.module.card.dto.CardResponse;
import az.hkbank.module.card.dto.CardSummaryResponse;
import az.hkbank.module.card.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Card entity conversions.
 */
@Mapper(componentModel = "spring", imports = CardNumberGenerator.class)
public interface CardMapper {

    /**
     * Converts Card entity to CardResponse DTO.
     * Card number is masked for security.
     *
     * @param card the card entity
     * @return CardResponse DTO
     */
    @Mapping(target = "maskedCardNumber", expression = "java(CardNumberGenerator.maskCardNumber(card.getCardNumber()))")
    @Mapping(target = "fullCardNumber", expression = "java(CardNumberGenerator.formatCardNumber(card.getCardNumber()))")
    @Mapping(target = "cvv", expression = "java(CardNumberGenerator.revealableCvv(card.getCvv()))")
    @Mapping(target = "accountId", source = "account.id")
    CardResponse toCardResponse(Card card);

    /**
     * Converts Card entity to CardSummaryResponse DTO.
     * Card number is masked for security.
     *
     * @param card the card entity
     * @return CardSummaryResponse DTO
     */
    @Mapping(target = "maskedCardNumber", expression = "java(CardNumberGenerator.maskCardNumber(card.getCardNumber()))")
    CardSummaryResponse toCardSummaryResponse(Card card);
}
