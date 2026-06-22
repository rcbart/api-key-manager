package com.apikeymanager.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RateLimiterServiceTest {

    private final UUID keyId = UUID.randomUUID();

    private static Clock fixedClockAt(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }

    @Test
    void allowsUnlimitedCallsWhenLimitIsNull() {
        RateLimiterService limiter = new RateLimiterService(fixedClockAt(Instant.parse("2026-01-01T00:00:00Z")));
        for (int i = 0; i < 1000; i++) {
            assertThat(limiter.tryAcquire(keyId, null)).isTrue();
        }
    }

    @Test
    void allowsUnlimitedCallsWhenLimitIsZeroOrNegative() {
        RateLimiterService limiter = new RateLimiterService(fixedClockAt(Instant.parse("2026-01-01T00:00:00Z")));
        assertThat(limiter.tryAcquire(keyId, 0)).isTrue();
        assertThat(limiter.tryAcquire(keyId, -5)).isTrue();
    }

    @Test
    void allowsExactlyTheConfiguredNumberOfCallsWithinAWindow() {
        RateLimiterService limiter = new RateLimiterService(fixedClockAt(Instant.parse("2026-01-01T00:00:00Z")));
        assertThat(limiter.tryAcquire(keyId, 3)).isTrue();
        assertThat(limiter.tryAcquire(keyId, 3)).isTrue();
        assertThat(limiter.tryAcquire(keyId, 3)).isTrue();
        assertThat(limiter.tryAcquire(keyId, 3)).isFalse();
        assertThat(limiter.tryAcquire(keyId, 3)).isFalse();
    }

    @Test
    void resetsTheCountInANewWindow() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        var mutableClock = new MutableClock(start);
        RateLimiterService limiter = new RateLimiterService(mutableClock);

        assertThat(limiter.tryAcquire(keyId, 1)).isTrue();
        assertThat(limiter.tryAcquire(keyId, 1)).isFalse();

        mutableClock.advance(Duration.ofMinutes(1));

        assertThat(limiter.tryAcquire(keyId, 1)).isTrue();
    }

    @Test
    void tracksDifferentKeysIndependently() {
        RateLimiterService limiter = new RateLimiterService(fixedClockAt(Instant.parse("2026-01-01T00:00:00Z")));
        UUID otherKeyId = UUID.randomUUID();

        assertThat(limiter.tryAcquire(keyId, 1)).isTrue();
        assertThat(limiter.tryAcquire(keyId, 1)).isFalse();
        assertThat(limiter.tryAcquire(otherKeyId, 1)).isTrue();
    }

    @Test
    void resetClearsTrackingSoTheNextCallStartsFresh() {
        RateLimiterService limiter = new RateLimiterService(fixedClockAt(Instant.parse("2026-01-01T00:00:00Z")));
        assertThat(limiter.tryAcquire(keyId, 1)).isTrue();
        assertThat(limiter.tryAcquire(keyId, 1)).isFalse();

        limiter.reset(keyId);

        assertThat(limiter.tryAcquire(keyId, 1)).isTrue();
    }

    /** A Clock test double whose "now" can be advanced between calls. */
    private static final class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            this.instant = this.instant.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
