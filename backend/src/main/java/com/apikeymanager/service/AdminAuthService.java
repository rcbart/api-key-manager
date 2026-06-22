package com.apikeymanager.service;

import com.apikeymanager.domain.AdminUser;
import com.apikeymanager.dto.LoginResponse;
import com.apikeymanager.exception.InvalidCredentialsException;
import com.apikeymanager.repository.AdminUserRepository;
import com.apikeymanager.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles admin login directly (rather than through Spring Security's
 * AuthenticationManager) -- it's a simple lookup + bcrypt check + JWT issue,
 * and keeping it explicit here is easier to read and test than wiring a
 * full UserDetailsService/AuthenticationProvider for what is just one
 * endpoint. JwtAuthFilter handles authenticating *subsequent* requests by
 * trusting a validly-signed token, without hitting the database again.
 */
@Service
public class AdminAuthService {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthService.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminAuthService(
            AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String username, String rawPassword) {
        AdminUser user = adminUserRepository.findByUsername(username).orElse(null);

        // Always run the bcrypt comparison, even with a dummy hash when the
        // user doesn't exist, so response timing doesn't reveal whether a
        // username is registered.
        String hashToCheck = user != null ? user.getPasswordHash() : DUMMY_HASH;
        boolean passwordMatches = passwordEncoder.matches(rawPassword, hashToCheck);

        if (user == null || !passwordMatches) {
            log.warn("admin login failed for username={}", username);
            throw new InvalidCredentialsException();
        }

        log.info("admin login succeeded for username={}", username);
        JwtService.IssuedToken issued = jwtService.issueToken(user.getUsername());
        return new LoginResponse(issued.token(), issued.expiresAt());
    }

    // A syntactically valid bcrypt hash of an unguessable, unused value --
    // exists only so passwordEncoder.matches() has constant-ish work to do
    // when the username doesn't exist.
    private static final String DUMMY_HASH =
            "$2a$10$WqRBfXKDXJ4t4xkjnu5p1.0z3jZcK3yT5Yt0p1m9b1aQzL0pE0p6S";
}
