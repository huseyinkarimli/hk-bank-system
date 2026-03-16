package az.hkbank.module.user;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.config.jwt.JwtService;
import az.hkbank.module.audit.service.AuditService;
import az.hkbank.module.user.dto.AuthResponse;
import az.hkbank.module.user.dto.LoginRequest;
import az.hkbank.module.user.dto.RegisterRequest;
import az.hkbank.module.user.entity.Role;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.user.mapper.UserMapper;
import az.hkbank.module.user.repository.UserRepository;
import az.hkbank.module.user.service.impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditService auditService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .phoneNumber("+994501234567")
                .build();

        loginRequest = LoginRequest.builder()
                .email("john.doe@example.com")
                .password("password123")
                .build();

        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("$2a$10$encodedPassword")
                .phoneNumber("+994501234567")
                .role(Role.USER)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())).thenReturn(false);
        when(userMapper.toUser(registerRequest)).thenReturn(user);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        AuthResponse response = userService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("john.doe@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals(Role.USER, response.getRole());

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository).existsByPhoneNumber(registerRequest.getPhoneNumber());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
        verify(auditService).log(eq(1L), anyString(), anyString(), anyString());
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsBankException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        BankException exception = assertThrows(BankException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals(ErrorCode.USER_ALREADY_EXISTS, exception.getErrorCode());
        assertEquals("Email already registered", exception.getDetail());

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_PhoneAlreadyExists_ThrowsBankException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())).thenReturn(true);

        BankException exception = assertThrows(BankException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals(ErrorCode.USER_ALREADY_EXISTS, exception.getErrorCode());
        assertEquals("Phone number already registered", exception.getDetail());

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository).existsByPhoneNumber(registerRequest.getPhoneNumber());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        AuthResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("john.doe@example.com", response.getEmail());

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
        verify(jwtService).generateToken(user);
        verify(auditService).log(eq(1L), anyString(), anyString(), anyString());
    }

    @Test
    void login_InvalidCredentials_ThrowsBankException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        BankException exception = assertThrows(BankException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void softDeleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        userService.softDeleteUser(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).save(argThat(u -> u.isDeleted()));
        verify(auditService).log(eq(1L), anyString(), anyString(), anyString());
    }

    @Test
    void softDeleteUser_UserNotFound_ThrowsBankException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BankException exception = assertThrows(BankException.class, () -> {
            userService.softDeleteUser(1L);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }
}
