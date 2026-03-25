package az.hkbank.module.user.service.impl;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.config.jwt.JwtService;
import az.hkbank.module.audit.service.AuditAction;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.user.dto.*;
import az.hkbank.module.user.entity.Role;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.notification.entity.NotificationType;
import az.hkbank.module.notification.service.NotificationService;
import az.hkbank.module.user.mapper.UserMapper;
import az.hkbank.module.user.repository.UserRepository;
import az.hkbank.module.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of UserService interface.
 * Handles user management operations and implements Spring Security's UserDetailsService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BankException(ErrorCode.USER_ALREADY_EXISTS, "Email already registered");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BankException(ErrorCode.USER_ALREADY_EXISTS, "Phone number already registered");
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        auditService.log(
                savedUser.getId(),
                AuditAction.REGISTER,
                "User registered successfully",
                getClientIpAddress()
        );

        log.info("User registered successfully: {}", savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("User attempting to login: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BankException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BankException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.isDeleted()) {
            throw new BankException(ErrorCode.USER_NOT_FOUND, "User account is deleted");
        }

        String token = jwtService.generateToken(user);

        String ipAddress = getClientIpAddress();

        auditService.log(
                user.getId(),
                AuditAction.LOGIN,
                "User logged in successfully",
                ipAddress
        );

        notificationService.createNotification(
                user.getId(),
                NotificationType.SECURITY,
                "Yeni giriş",
                "Hesabınıza yeni giriş edildi: " + ipAddress
        );

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BankException(ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new BankException(ErrorCode.USER_NOT_FOUND, "User is deleted");
        }

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BankException(ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new BankException(ErrorCode.USER_NOT_FOUND, "User is deleted");
        }

        if (!user.getPhoneNumber().equals(request.getPhoneNumber()) &&
                userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BankException(ErrorCode.USER_ALREADY_EXISTS, "Phone number already in use");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());

        User updatedUser = userRepository.save(user);

        auditService.log(
                user.getId(),
                AuditAction.USER_UPDATE,
                "User profile updated",
                getClientIpAddress()
        );

        log.info("User updated successfully: {}", id);

        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void softDeleteUser(Long id) {
        log.info("Soft deleting user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BankException(ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new BankException(ErrorCode.USER_NOT_FOUND, "User is already deleted");
        }

        user.setDeleted(true);
        userRepository.save(user);

        auditService.log(
                user.getId(),
                AuditAction.USER_DELETE,
                "User account soft deleted",
                getClientIpAddress()
        );

        log.info("User soft deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all active users");

        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private String getClientIpAddress() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return httpServletRequest.getRemoteAddr();
    }
}
