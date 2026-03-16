package az.hkbank.module.user.entity;

import org.springframework.security.core.GrantedAuthority;

/**
 * User role enumeration for the HK Bank System.
 * Implements GrantedAuthority for Spring Security integration.
 */
public enum Role implements GrantedAuthority {
    USER,
    ADMIN,
    AI_SUPPORT;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
