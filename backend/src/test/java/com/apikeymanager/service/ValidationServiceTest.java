package com.apikeymanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.apikeymanager.domain.ApiKey;
import com.apikeymanager.dto.ValidateKeyResponse;
import com.apikeymanager.repository.ApiKeyRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-01T00:00:00Z");

    @Mock
    private ApiKeyRepository apiKeyRepository;

    private final KeyGenerationService keyGenerationService = new KeyGenerationService("ak_live_", 6);
    private RateLimiterService rateLimiterService;
    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        rateLimiterService = new RateLimiterService(clock);
        validationService = new ValidationService(apiKeyRepository, keyGenerationService, rateLimiterService, clock);
    }

    private ApiKey buildKey(Set<String> scopes, Set<String> allowedIps, Integer rateLimit, Instant expiresAt) {
        return new ApiKey(
                UUID.randomUUID(),
                "Test key",
                "ak_live_abcdef",
                "irrelevant-hash-overwritten-by-mock",
                scopes,
                allowedIps,
                rateLimit,
                expiresAt,
                "admin",
                NOW.minusSeconds(3600));
    }

    @Test
    void unknownKeyIsInvalid() {
        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.empty());

        ValidateKeyResponse result = validationService.validate("ak_live_doesnotexist", "1.2.3.4", null);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getReason()).isEqualTo("invalid_key");
    }

    @Test
    void blankKeyIsInvalidWithoutHittingTheRepository() {
        ValidateKeyResponse result = validationService.validate("", "1.2.3.4", null);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getReason()).isEqualTo("missing_key");
    }

    @Test
    void revokedKeyIsInvalid() {
        ApiKey key = buildKey(Set.of(), Set.of(), null, null);
        key.revoke(NOW.minusSeconds(60));
        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.of(key));

        ValidateKeyResponse result = validationService.validate("ak_live_x", null, null);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getReason()).isEqualTo("revoked");
    }

    @Test
    void expiredKeyIsInvalid() {
        ApiKey key = buildKey(Set.of(), Set.of(), null, NOW.minusSeconds(60));
        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.of(key));

        ValidateKeyResponse result = validationService.validate("ak_live_x", null, null);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getReason()).isEqualTo("expired");
    }

    @Test
    void notYetExpiredKeyIsValid() {
        ApiKey key = buildKey(Set.of(), Set.of(), null, NOW.plusSeconds(60));
        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.of(key));

        ValidateKeyResponse result = validationService.validate("ak_live_x", null, null);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void rateLimitedAfterTheConfiguredNumberOfCalls() {
        ApiKey key = buildKey(Set.of(), Set.of(), 1, null);
        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.of(key));

        assertThat(validationService.validate("ak_live_x", null, null).isValid()).isTrue();

        ValidateKeyResponse second = validationService.validate("ak_live_x", null, null);
        assertThat(second.isValid()).isFalse();
        assertThat(second.getReason()).isEqualTo("rate_limited");
    }

    @Test
    void sourceIpOutsideAllowlistIsInvalid() {
        ApiKey key = buildKey(Set.of(), Set.of("10.0.0.0/8"), null, null);
        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.of(key));

        ValidateKeyResponse result = validationService.validate("ak_live_x", "192.168.1.1", null);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getReason()).isEqualTo("ip_not_allowed");
    }

    @Test
    void sourceIpInsideAllowlistIsValid() {
        ApiKey key = buildKey(Set.of(), Set.of("10.0.0.0/8"), null, null);
        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.of(key));

        ValidateKeyResponse result = validationService.validate("ak_live_x", "10.1.2.3", null);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void emptyAllowlistMeansAnySourceIpIsFine() {
        ApiKey key = buildKey(Set.of(), Set.of(), null, null);
        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.of(key));

        ValidateKeyResponse result = validationService.validate("ak_live_x", "203.0.113.5", null);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void missingRequiredScopeIsInvalid() {
        ApiKey key = buildKey(Set.of("read:orders"), Set.of(), null, null);
        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.of(key));

        ValidateKeyResponse result = validationService.validate("ak_live_x", null, "write:orders");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getReason()).isEqualTo("insufficient_scope");
    }

    @Test
    void presentRequiredScopeIsValidAndReturnsKeyMetadata() {
        ApiKey key = buildKey(Set.of("read:orders"), Set.of(), null, null);
        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.of(key));

        ValidateKeyResponse result = validationService.validate("ak_live_x", null, "read:orders");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getName()).isEqualTo("Test key");
        assertThat(result.getScopes()).containsExactly("read:orders");
    }

    @Test
    void successfulValidationUpdatesLastUsedAt() {
        ApiKey key = buildKey(Set.of(), Set.of(), null, null);
        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.of(key));

        assertThat(key.getLastUsedAt()).isNull();
        validationService.validate("ak_live_x", null, null);
        assertThat(key.getLastUsedAt()).isEqualTo(NOW);
    }
}
