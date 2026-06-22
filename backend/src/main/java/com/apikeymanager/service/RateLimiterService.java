package com.apikeymanager.service;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

/**
 * Simple in-memory, per-key, fixed-window rate limiter (window = one
 * calendar minute). Deliberately not backed by a shared store -- see
 * docs/SECURITY.md's "known limitations": this only works correctly for a
 * single backend instance. Swap the {@code ConcurrentHashMap} below for
 * Redis (or Bucket4j with a Redis backend) before running more than one
 * replica with rate limiting turned on.
 */
@Service
public class RateLimiterService {

    private record Window(long windowEpochMinute, AtomicInteger count) {
    }

    private final ConcurrentHashMap<UUID, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    public RateLimiterService() {
        this(Clock.systemUTC());
    }

    // Package-private constructor for tests that need to control time.
    RateLimiterService(Clock clock) {
        this.clock = clock;
    }

    /**
     * Returns true if this call is allowed under {@code limitPerMinute},
     * and records the call. A null/non-positive limit means unlimited.
     */
    public boolean tryAcquire(UUID keyId, Integer limitPerMinute) {
        if (limitPerMinute == null || limitPerMinute <= 0) {
            return true;
        }

        long currentMinute = clock.instant().getEpochSecond() / 60;

        Window window = windows.compute(keyId, (id, existing) -> {
            if (existing == null || existing.windowEpochMinute() != currentMinute) {
                return new Window(currentMinute, new AtomicInteger(1));
            }
            existing.count().incrementAndGet();
            return existing;
        });

        return window.count().get() <= limitPerMinute;
    }

    /** Removes tracking state for a key (e.g. after it's revoked/deleted), so memory doesn't grow unbounded. */
    public void reset(UUID keyId) {
        windows.remove(keyId);
    }
}
