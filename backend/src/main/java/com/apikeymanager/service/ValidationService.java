package com.apikeymanager.service;

import com.apikeymanager.domain.ApiKey;
import com.apikeymanager.dto.ValidateKeyResponse;
import com.apikeymanager.repository.ApiKeyRepository;
import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The actual "is this key allowed to do this" check, called by other
 * services through POST /api/validate (see ValidationController). Checks
 * run cheapest-and-most-decisive first: existence, revocation, expiration,
 * rate limit, IP allowlist, scope.
 */
@Service
public class ValidationService {

    private static final Logger log = LoggerFactory.getLogger(ValidationService.class);

    private final ApiKeyRepository apiKeyRepository;
    private final KeyGenerationService keyGenerationService;
    private final RateLimiterService rateLimiterService;
    private final Clock clock;

    public ValidationService(
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
    public ValidateKeyResponse validate(String rawKey, String sourceIp, String requiredScope) {
        if (rawKey == null || rawKey.isBlank()) {
            return ValidateKeyResponse.invalid("missing_key");
        }

        String keyHash = keyGenerationService.hash(rawKey);
        Optional<ApiKey> found = apiKeyRepository.findByKeyHash(keyHash);

        if (found.isEmpty()) {
            log.warn("validation failed: unknown key");
            return ValidateKeyResponse.invalid("invalid_key");
        }

        ApiKey apiKey = found.get();

        if (apiKey.isRevoked()) {
            log.warn("validation failed: revoked key id={}", apiKey.getId());
            return ValidateKeyResponse.invalid("revoked");
        }

        if (apiKey.isExpired(clock.instant())) {
            log.warn("validation failed: expired key id={}", apiKey.getId());
            return ValidateKeyResponse.invalid("expired");
        }

        if (!rateLimiterService.tryAcquire(apiKey.getId(), apiKey.getRateLimitPerMinute())) {
            log.warn("validation failed: rate limited key id={}", apiKey.getId());
            return ValidateKeyResponse.invalid("rate_limited");
        }

        if (!apiKey.getAllowedIps().isEmpty()) {
            boolean ipAllowed = sourceIp != null
                    && apiKey.getAllowedIps().stream().anyMatch(entry -> IpMatcher.matches(sourceIp, entry));
            if (!ipAllowed) {
                log.warn("validation failed: source IP not allowed key id={}", apiKey.getId());
                return ValidateKeyResponse.invalid("ip_not_allowed");
            }
        }

        if (requiredScope != null && !requiredScope.isBlank() && !apiKey.getScopes().contains(requiredScope)) {
            log.warn("validation failed: insufficient scope key id={} required={}", apiKey.getId(), requiredScope);
            return ValidateKeyResponse.invalid("insufficient_scope");
        }

        apiKey.touchLastUsed(clock.instant());
        return ValidateKeyResponse.valid(apiKey.getName(), apiKey.getScopes(), apiKey.getExpiresAt());
    }
}
