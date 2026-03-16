package az.hkbank.module.ai.controller;

import az.hkbank.common.response.ApiResponse;
import az.hkbank.module.ai.dto.ChatMessageResponse;
import az.hkbank.module.ai.dto.ChatSessionResponse;
import az.hkbank.module.ai.dto.SendMessageRequest;
import az.hkbank.module.ai.service.AiSupportService;
import az.hkbank.module.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for AI support operations.
 * Handles chat session management and AI assistant interactions.
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "AI Support", description = "AI assistant chat and support endpoints")
public class AiSupportController {

    private final AiSupportService aiSupportService;

    /**
     * Starts a new chat session.
     *
     * @param currentUser the authenticated user
     * @return ApiResponse containing new session details
     */
    @PostMapping("/sessions")
    @Operation(summary = "Start new chat session", description = "Creates a new AI chat session for the user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Session created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<ApiResponse<ChatSessionResponse>> startSession(
            @AuthenticationPrincipal User currentUser) {
        ChatSessionResponse session = aiSupportService.startSession(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Chat session started", session));
    }

    /**
     * Sends a message in a chat session.
     *
     * @param sessionId the session ID
     * @param request the message request
     * @param currentUser the authenticated user
     * @return ApiResponse containing AI response
     */
    @PostMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "Send message", description = "Sends a message to the AI assistant and receives a response")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Message sent successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Session not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "503",
                    description = "AI service unavailable"
            )
    })
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable String sessionId,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal User currentUser) {
        ChatMessageResponse response = aiSupportService.sendMessage(
                currentUser.getId(),
                sessionId,
                request.getMessage()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves all chat sessions for the user.
     *
     * @param currentUser the authenticated user
     * @return ApiResponse containing list of sessions
     */
    @GetMapping("/sessions")
    @Operation(summary = "Get user sessions", description = "Retrieves all chat sessions for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Sessions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<ApiResponse<List<ChatSessionResponse>>> getUserSessions(
            @AuthenticationPrincipal User currentUser) {
        List<ChatSessionResponse> sessions = aiSupportService.getUserSessions(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    /**
     * Retrieves message history for a session.
     *
     * @param sessionId the session ID
     * @param currentUser the authenticated user
     * @return ApiResponse containing message history
     */
    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "Get session history", description = "Retrieves all messages in a chat session")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "History retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Session not found"
            )
    })
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getSessionHistory(
            @PathVariable String sessionId,
            @AuthenticationPrincipal User currentUser) {
        List<ChatMessageResponse> messages = aiSupportService.getSessionHistory(
                currentUser.getId(),
                sessionId
        );
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    /**
     * Closes a chat session.
     *
     * @param sessionId the session ID
     * @param currentUser the authenticated user
     * @return ApiResponse with success message
     */
    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "Close session", description = "Closes an active chat session")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Session closed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Session not found"
            )
    })
    public ResponseEntity<ApiResponse<Void>> closeSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal User currentUser) {
        aiSupportService.closeSession(currentUser.getId(), sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session closed successfully", null));
    }
}
