package az.hkbank.module.ai.mapper;

import az.hkbank.module.ai.dto.ChatMessageResponse;
import az.hkbank.module.ai.dto.ChatSessionResponse;
import az.hkbank.module.ai.entity.ChatMessage;
import az.hkbank.module.ai.entity.ChatSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for AI chat entity transformations.
 * Converts between chat entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface ChatMapper {

    /**
     * Converts ChatSession entity to ChatSessionResponse DTO.
     *
     * @param session the chat session entity
     * @return chat session response DTO
     */
    @Mapping(target = "messageCount", constant = "0")
    ChatSessionResponse toChatSessionResponse(ChatSession session);

    /**
     * Converts ChatMessage entity to ChatMessageResponse DTO.
     *
     * @param message the chat message entity
     * @return chat message response DTO
     */
    ChatMessageResponse toChatMessageResponse(ChatMessage message);
}
