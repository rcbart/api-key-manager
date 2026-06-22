package com.apikeymanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.apikeymanager.domain.AdminUser;
import com.apikeymanager.dto.LoginResponse;
import com.apikeymanager.exception.InvalidCredentialsException;
import com.apikeymanager.repository.AdminUserRepository;
import com.apikeymanager.security.JwtService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AdminAuthService adminAuthService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        adminAuthService = new AdminAuthService(adminUserRepository, passwordEncoder, jwtService);
    }

    @Test
    void successfulLoginReturnsAnIssuedToken() {
        AdminUser user = new AdminUser(UUID.randomUUID(), "alice", "hashed-password", Instant.now());
        when(adminUserRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);
        when(jwtService.issueToken("alice"))
                .thenReturn(new JwtService.IssuedToken("a.jwt.token", Instant.parse("2026-06-02T00:00:00Z")));

        LoginResponse response = adminAuthService.login("alice", "correct-password");

        assertThat(response.getToken()).isEqualTo("a.jwt.token");
        assertThat(response.getExpiresAt()).isEqualTo(Instant.parse("2026-06-02T00:00:00Z"));
    }

    @Test
    void wrongPasswordThrowsInvalidCredentials() {
        AdminUser user = new AdminUser(UUID.randomUUID(), "alice", "hashed-password", Instant.now());
        when(adminUserRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> adminAuthService.login("alice", "wrong-password"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void unknownUsernameThrowsInvalidCredentialsWithoutRevealingThatTheUserIsMissing() {
        when(adminUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> adminAuthService.login("ghost", "anything"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");

        // Still runs a bcrypt comparison even though the user doesn't exist
        // -- this is what keeps "unknown user" and "wrong password" from
        // being distinguishable by response timing.
        verify(passwordEncoder, times(1)).matches(eq("anything"), any());
    }

    @Test
    void jwtIsNeverIssuedOnAFailedLogin() {
        when(adminUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> adminAuthService.login("ghost", "anything"))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).issueToken(any());
    }
}
