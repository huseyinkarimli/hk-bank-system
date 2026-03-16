package az.hkbank.module.ai.dto;

import az.hkbank.module.ai.entity.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for chat message details.
 * Contains message information for display to users.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    private Long id;
    private MessageRole role;
    private String content;
    private LocalDateTime createdAt;
}
