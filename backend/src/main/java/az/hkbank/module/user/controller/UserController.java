package az.hkbank.module.user.controller;

import az.hkbank.common.response.ApiResponse;
import az.hkbank.module.user.dto.UpdateUserRequest;
import az.hkbank.module.user.dto.UserResponse;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user management operations.
 * Handles user profile operations and admin user management endpoints.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Management", description = "User profile and management endpoints")
public class UserController {

    private final UserService userService;

    /**
     * Retrieves the current authenticated user's profile.
     *
     * @param currentUser the authenticated user
     * @return ApiResponse containing user information
     */
    @GetMapping("/users/me")
    @Operation(summary = "Get current user profile", description = "Returns the authenticated user's profile information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        UserResponse userResponse = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    /**
     * Updates the current authenticated user's profile.
     *
     * @param currentUser the authenticated user
     * @param request the update request
     * @return ApiResponse containing updated user information
     */
    @PutMapping("/users/me")
    @Operation(summary = "Update current user profile", description = "Updates the authenticated user's profile information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse userResponse = userService.updateUser(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", userResponse));
    }

    /**
     * Soft deletes the current authenticated user's account.
     *
     * @param currentUser the authenticated user
     * @return ApiResponse with success message
     */
    @DeleteMapping("/users/me")
    @Operation(summary = "Delete current user account", description = "Soft deletes the authenticated user's account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Account deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteCurrentUser(@AuthenticationPrincipal User currentUser) {
        userService.softDeleteUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }
}
