package az.hkbank.module.user.controller;

import az.hkbank.common.response.ApiResponse;
import az.hkbank.config.jwt.JwtService;
import az.hkbank.module.user.dto.AuthResponse;
import az.hkbank.module.user.dto.LoginRequest;
import az.hkbank.module.user.dto.RefreshTokenRequest;
import az.hkbank.module.user.dto.RegisterRequest;
import az.hkbank.module.user.entity.RefreshToken;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.user.service.RefreshTokenService;
import az.hkbank.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication operations.
 * Handles user registration and login endpoints.
 */
@RestController
@RequestMapping({"/api/auth", "/apis"})
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Registers a new user in the system.
     *
     * @param request the registration request
     * @return ApiResponse containing authentication token and user information
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns JWT token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "User already exists"
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = userService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authResponse));
    }

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param request the login request
     * @return ApiResponse containing authentication token and user information
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns JWT token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * Issues a new access token and refresh token using a valid refresh token.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Rotates refresh token and returns new JWT pair")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Tokens refreshed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token"
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshToken validated = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        User user = validated.getUser();

        String accessToken = jwtService.generateToken(user);
        String newRefresh = refreshTokenService.generateRefreshToken(user);

        AuthResponse authResponse = AuthResponse.builder()
                .token(accessToken)
                .refreshToken(newRefresh)
                .tokenType("Bearer")
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }
}
