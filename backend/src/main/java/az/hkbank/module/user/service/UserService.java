package az.hkbank.module.user.service;

import az.hkbank.module.user.dto.*;

import java.util.List;

/**
 * Service interface for user management operations.
 * Defines business logic for user registration, authentication, and profile management.
 */
public interface UserService {

    /**
     * Registers a new user in the system.
     *
     * @param request the registration request
     * @return authentication response with JWT token
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param request the login request
     * @return authentication response with JWT token
     */
    AuthResponse login(LoginRequest request);

    /**
     * Retrieves a user by ID.
     *
     * @param id the user ID
     * @return user response DTO
     */
    UserResponse getUserById(Long id);

    /**
     * Updates a user's profile information.
     *
     * @param id the user ID
     * @param request the update request
     * @return updated user response DTO
     */
    UserResponse updateUser(Long id, UpdateUserRequest request);

    /**
     * Soft deletes a user (sets isDeleted flag).
     *
     * @param id the user ID
     */
    void softDeleteUser(Long id);

    /**
     * Retrieves all active users (admin only).
     *
     * @return list of user response DTOs
     */
    List<UserResponse> getAllUsers();
}
