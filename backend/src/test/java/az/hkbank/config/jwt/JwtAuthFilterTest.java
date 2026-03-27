package az.hkbank.config.jwt;

import az.hkbank.module.user.entity.Role;
import az.hkbank.module.user.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * JWT filter behaviour for banned users.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void bannedUser_receives401_andChainNotContinuedWithAuth() throws Exception {
        SecurityContextHolder.clearContext();

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtService.extractEmail("token")).thenReturn("u@hkbank.az");

        User user = User.builder()
                .id(42L)
                .firstName("A")
                .lastName("B")
                .email("u@hkbank.az")
                .password("p")
                .phoneNumber("+994000000000")
                .role(Role.USER)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userDetailsService.loadUserByUsername("u@hkbank.az")).thenReturn(user);
        when(jwtService.extractUserId("token")).thenReturn(42L);
        when(jwtService.isUserBanned(42L)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(jwtService, never()).isTokenValid(any(), any());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void expiredToken_Returns401WithJson() throws Exception {
        SecurityContextHolder.clearContext();

        when(request.getHeader("Authorization")).thenReturn("Bearer expired");
        when(jwtService.extractEmail("expired")).thenThrow(mock(ExpiredJwtException.class));

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
        assertTrue(sw.toString().contains("Token müddəti bitmişdir"));
    }

    @Test
    void malformedToken_Returns401WithJson() throws Exception {
        SecurityContextHolder.clearContext();

        when(request.getHeader("Authorization")).thenReturn("Bearer bad");
        when(jwtService.extractEmail("bad")).thenThrow(mock(JwtException.class));

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
        assertTrue(sw.toString().contains("Token etibarsızdır"));
    }
}
