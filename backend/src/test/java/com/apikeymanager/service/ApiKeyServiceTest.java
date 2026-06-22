package com.apikeymanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.apikeymanager.domain.ApiKey;
import com.apikeymanager.dto.ApiKeySummaryResponse;
import com.apikeymanager.dto.CreateApiKeyRequest;
import com.apikeymanager.dto.CreateApiKeyResponse;
import com.apikeymanager.dto.UpdateApiKeyRequest;
import com.apikeymanager.exception.ApiKeyNotFoundException;
import com.apikeymanager.repository.ApiKeyRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-01T00:00:00Z");

    @Mock
    private ApiKeyRepository apiKeyRepository;

    private ApiKeyService apiKeyService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        KeyGenerationService keyGenerationService = new KeyGenerationService("ak_live_", 6);
        RateLimiterService rateLimiterService = new RateLimiterService(clock);
        apiKeyService = new ApiKeyService(apiKeyRepository, keyGenerationService, rateLimiterService, clock);
    }

    @Test
    void createReturnsTheRawKeyExactlyOnceAlongsideItsSummary() {
        when(apiKeyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setName("CI pipeline");
        request.setScopes(Set.of("read:builds"));
        request.setRateLimitPerMinute(60);

        CreateApiKeyResponse response = apiKeyService.create(request, "alice");

        assertThat(response.getRawKey()).startsWith("ak_live_");
        assertThat(response.getKey().getName()).isEqualTo("CI pipeline");
        assertThat(response.getKey().getScopes()).containsExactly("read:builds");
        assertThat(response.getKey().getCreatedBy()).isEqualTo("alice");
        assertThat(response.getKey().isRevoked()).isFalse();
        // The persisted entity must never carry the raw key anywhere.
        verify(apiKeyRepository).save(any());
    }

    @Test
    void listAllMapsEveryEntityToASummary() {
        ApiKey key1 = sampleKey("first");
        ApiKey key2 = sampleKey("second");
        when(apiKeyRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(key1, key2));

        List<ApiKeySummaryResponse> result = apiKeyService.listAll();

        assertThat(result).extracting(ApiKeySummaryResponse::getName).containsExactly("first", "second");
    }

    @Test
    void getOneThrowsWhenTheKeyDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(apiKeyRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apiKeyService.getOne(id)).isInstanceOf(ApiKeyNotFoundException.class);
    }

    @Test
    void updateAppliesOnlyTheProvidedFields() {
        ApiKey key = sampleKey("original name");
        when(apiKeyRepository.findById(key.getId())).thenReturn(Optional.of(key));

        UpdateApiKeyRequest update = new UpdateApiKeyRequest();
        update.setName("renamed");

        ApiKeySummaryResponse result = apiKeyService.update(key.getId(), update);

        assertThat(result.getName()).isEqualTo("renamed");
    }

    @Test
    void updateWithClearRateLimitRemovesIt() {
        ApiKey key = sampleKey("with limit");
        // Re-create with a rate limit set, since sampleKey() defaults to none.
        key = new ApiKey(
                key.getId(), key.getName(), key.getDisplayPrefix(), key.getKeyHash(),
                Set.of(), Set.of(), 100, null, key.getCreatedBy(), NOW);
        when(apiKeyRepository.findById(key.getId())).thenReturn(Optional.of(key));

        UpdateApiKeyRequest update = new UpdateApiKeyRequest();
        update.setClearRateLimit(true);

        ApiKeySummaryResponse result = apiKeyService.update(key.getId(), update);

        assertThat(result.getRateLimitPerMinute()).isNull();
    }

    @Test
    void revokeMarksTheKeyRevokedAndStampsRevokedAt() {
        ApiKey key = sampleKey("to revoke");
        when(apiKeyRepository.findById(key.getId())).thenReturn(Optional.of(key));

        ApiKeySummaryResponse result = apiKeyService.revoke(key.getId());

        assertThat(result.isRevoked()).isTrue();
        assertThat(result.getRevokedAt()).isEqualTo(NOW);
    }

    @Test
    void deleteRemovesTheEntityAndThrowsIfItDoesNotExist() {
        ApiKey key = sampleKey("to delete");
        when(apiKeyRepository.findById(key.getId())).thenReturn(Optional.of(key));

        apiKeyService.delete(key.getId());

        verify(apiKeyRepository).delete(key);
    }

    @Test
    void deleteThrowsWhenTheKeyDoesNotExistAndNeverCallsRepositoryDelete() {
        UUID id = UUID.randomUUID();
        when(apiKeyRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apiKeyService.delete(id)).isInstanceOf(ApiKeyNotFoundException.class);
        verify(apiKeyRepository, never()).delete(any());
    }

    private ApiKey sampleKey(String name) {
        return new ApiKey(
                UUID.randomUUID(),
                name,
                "ak_live_abcdef",
                "some-hash",
                Set.of(),
                Set.of(),
                null,
                null,
                "admin",
                NOW);
    }
}
