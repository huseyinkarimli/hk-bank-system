package az.hkbank.config.jwt;

import az.hkbank.module.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Service for JWT token generation and validation.
 * Handles token creation, parsing, and validation operations.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE = "role";

    private final JwtProperties jwtProperties;

    private final Set<Long> bannedUserIds = ConcurrentHashMap.newKeySet();

    /**
     * Generates a JWT token for the given user.
     *
     * @param userDetails the user details
     * @return JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User user) {
            claims.put(CLAIM_USER_ID, user.getId());
            claims.put(CLAIM_ROLE, user.getRole().name());
        }
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Extracts user id from token claims, if present (tokens issued before this claim may return null).
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object uid = claims.get(CLAIM_USER_ID);
        if (uid == null) {
            return null;
        }
        if (uid instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    /**
     * Marks a user as banned for JWT validation; existing tokens are rejected until server restart
     * (in-memory set) or explicit unban.
     */
    public void banUser(Long userId) {
        bannedUserIds.add(userId);
    }

    public boolean isUserBanned(Long userId) {
        return bannedUserIds.contains(userId);
    }

    /**
     * Extracts the email (username) from the JWT token.
     *
     * @param token the JWT token
     * @return email address
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the JWT token.
     *
     * @param token the JWT token
     * @return expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Validates the JWT token against the user details.
     *
     * @param token the JWT token
     * @param userDetails the user details
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
