package az.hkbank.module.card.controller;

import az.hkbank.common.response.ApiResponse;
import az.hkbank.module.card.dto.*;
import az.hkbank.module.card.service.CardService;
import az.hkbank.module.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for card management operations.
 * Handles card creation, retrieval, and management endpoints.
 */
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Card Management", description = "Card creation and management endpoints")
public class CardController {

    private final CardService cardService;

    /**
     * Creates a new card for the authenticated user.
     *
     * @param currentUser the authenticated user
     * @param request the card creation request
     * @return ApiResponse containing created card information
     */
    @PostMapping
    @Operation(summary = "Create new card", description = "Creates a new bank card for the authenticated user's account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Card created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error or card limit exceeded"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access to account"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    public ResponseEntity<ApiResponse<CardResponse>> createCard(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateCardRequest request) {
        CardResponse cardResponse = cardService.createCard(currentUser.getId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Card created successfully", cardResponse));
    }

    /**
     * Retrieves all cards for the authenticated user.
     *
     * @param currentUser the authenticated user
     * @return ApiResponse containing list of user cards
     */
    @GetMapping
    @Operation(summary = "Get user cards", description = "Retrieves all cards for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cards retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<ApiResponse<List<CardSummaryResponse>>> getUserCards(
            @AuthenticationPrincipal User currentUser) {
        List<CardSummaryResponse> cards = cardService.getUserCards(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    /**
     * Retrieves a specific card by ID.
     *
     * @param id the card ID
     * @param currentUser the authenticated user
     * @return ApiResponse containing card information
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get card by ID", description = "Retrieves a specific card by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Card retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access to card"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Card not found"
            )
    })
    public ResponseEntity<ApiResponse<CardResponse>> getCardById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        CardResponse cardResponse = cardService.getCardById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(cardResponse));
    }

    /**
     * Retrieves all cards for a specific account.
     *
     * @param accountId the account ID
     * @param currentUser the authenticated user
     * @return ApiResponse containing list of cards
     */
    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get cards by account", description = "Retrieves all cards for a specific account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cards retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access to account"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    public ResponseEntity<ApiResponse<List<CardSummaryResponse>>> getCardsByAccount(
            @PathVariable Long accountId,
            @AuthenticationPrincipal User currentUser) {
        List<CardSummaryResponse> cards = cardService.getCardsByAccount(accountId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    /**
     * Updates card status.
     *
     * @param id the card ID
     * @param request the status update request
     * @param currentUser the authenticated user
     * @return ApiResponse containing updated card information
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Update card status", description = "Updates card status (block/freeze/activate)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Card status updated successfully",
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
                    responseCode = "403",
                    description = "Unauthorized access to card"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Card not found"
            )
    })
    public ResponseEntity<ApiResponse<CardResponse>> updateCardStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCardStatusRequest request,
            @AuthenticationPrincipal User currentUser) {
        CardResponse cardResponse = cardService.updateCardStatus(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Card status updated successfully", cardResponse));
    }

    /**
     * Changes card PIN.
     *
     * @param id the card ID
     * @param request the PIN change request
     * @param currentUser the authenticated user
     * @return ApiResponse with success message
     */
    @PutMapping("/{id}/pin")
    @Operation(summary = "Change card PIN", description = "Changes the PIN for a card")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "PIN changed successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized or invalid PIN"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Card is blocked or unauthorized access"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Card not found"
            )
    })
    public ResponseEntity<ApiResponse<Void>> changePin(
            @PathVariable Long id,
            @Valid @RequestBody ChangePinRequest request,
            @AuthenticationPrincipal User currentUser) {
        cardService.changePin(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("PIN changed successfully", null));
    }

    /**
     * Soft deletes a card.
     *
     * @param id the card ID
     * @param currentUser the authenticated user
     * @return ApiResponse with success message
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete card", description = "Soft deletes a card")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Card deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access to card"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Card not found"
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteCard(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        cardService.softDeleteCard(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Card deleted successfully", null));
    }

    /**
     * Retrieves all cards (admin only).
     *
     * @return ApiResponse containing list of all cards
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all cards", description = "Retrieves all active cards (admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cards retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin access required"
            )
    })
    public ResponseEntity<ApiResponse<List<CardSummaryResponse>>> getAllCards() {
        List<CardSummaryResponse> cards = cardService.getAllCards();
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    /**
     * Updates card status (admin only).
     *
     * @param id the card ID
     * @param request the status update request
     * @return ApiResponse containing updated card information
     */
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin update card status", description = "Updates card status (admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Card status updated successfully",
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
                    responseCode = "403",
                    description = "Forbidden - Admin access required"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Card not found"
            )
    })
    public ResponseEntity<ApiResponse<CardResponse>> adminUpdateCardStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCardStatusRequest request) {
        CardResponse cardResponse = cardService.adminUpdateCardStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Card status updated successfully", cardResponse));
    }
}
