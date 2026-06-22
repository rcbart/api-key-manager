package com.apikeymanager.service;

import com.apikeymanager.domain.ApiKey;
import com.apikeymanager.dto.CreateApiKeyRequest;
import com.apikeymanager.dto.CreateApiKeyResponse;
import com.apikeymanager.dto.ApiKeySummaryResponse;
import com.apikeymanager.dto.UpdateApiKeyRequest;
import com.apikeymanager.exception.ApiKeyNotFoundException;
import com.apikeymanager.repository.ApiKeyRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin-facing CRUD for API keys. Every method here is reachable only
 * through JWT-protected /api/admin/** endpoints (see SecurityConfig) --
 * this class assumes the caller is already an authenticated admin.
 */
@Service
public class ApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyService.class);

    private final ApiKeyRepository apiKeyRepository;
    private final KeyGenerationService keyGenerationService;
    private final RateLimiterService rateLimiterService;
    private final Clock clock;

    public ApiKeyService(
            ApiKeyRepository apiKeyRepository,
            KeyGenerationService keyGenerationService,
            RateLimiterService rateLimiterService,
            Clock clock) {
        this.apiKeyRepository = apiKeyRepository;
        this.keyGenerationService = keyGenerationService;
        this.rateLimiterService = rateLimiterService;
        this.clock = clock;
    }

    @Transactional
    public CreateApiKeyResponse create(CreateApiKeyRequest request, String createdBy) {
        KeyGenerationService.GeneratedKey generated = keyGenerationService.generate();

        ApiKey apiKey = new ApiKey(
                UUID.randomUUID(),
                request.getName(),
                generated.displayPrefix(),
                generated.keyHash(),
                request.getScopes(),
                request.getAllowedIps(),
                request.getRateLimitPerMinute(),
                request.getExpiresAt(),
                createdBy,
                clock.instant());

        apiKey = apiKeyRepository.save(apiKey);

        // Never log the raw key -- only the id/display prefix, which can't
        // be used to authenticate.
        log.info("API key created id={} displayPrefix={} createdBy={}", apiKey.getId(), apiKey.getDisplayPrefix(), createdBy);

        return new CreateApiKeyResponse(generated.rawKey(), ApiKeySummaryResponse.from(apiKey));
    }

    @Transactional(readOnly = true)
    public List<ApiKeySummaryResponse> listAll() {
        return apiKeyRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ApiKeySummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApiKeySummaryResponse getOne(UUID id) {
        return ApiKeySummaryResponse.from(getOrThrow(id));
    }

    @Transactional
    public ApiKeySummaryResponse update(UUID id, UpdateApiKeyRequest request) {
        ApiKey apiKey = getOrThrow(id);

        apiKey.applyUpdate(
                request.getName(),
                request.getScopes(),
                request.getAllowedIps(),
                request.isClearRateLimit() ? null : request.getRateLimitPerMinute(),
                request.isClearExpiresAt() ? null : request.getExpiresAt(),
                clock.instant());

        log.info("API key updated id={}", id);
        return ApiKeySummaryResponse.from(apiKey);
    }

    @Transactional
    public ApiKeySummaryResponse revoke(UUID id) {
        ApiKey apiKey = getOrThrow(id);
        apiKey.revoke(clock.instant());
        rateLimiterService.reset(id);
        log.info("API key revoked id={}", id);
        return ApiKeySummaryResponse.from(apiKey);
    }

    @Transactional
    public void delete(UUID id) {
        ApiKey apiKey = getOrThrow(id);
        apiKeyRepository.delete(apiKey);
        rateLimiterService.reset(id);
        log.info("API key deleted id={}", id);
    }

    private ApiKey getOrThrow(UUID id) {
        return apiKeyRepository.findById(id).orElseThrow(() -> new ApiKeyNotFoundException(id));
    }
}
