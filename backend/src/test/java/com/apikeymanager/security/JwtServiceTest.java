package com.apikeymanager.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-at-least-32-bytes-long!!";
    private static final String OTHER_SECRET = "a-completely-different-secret-key-32bytes";

    @Test
    void issuedTokenParsesBackToTheSameUsername() {
        JwtService jwtService = new JwtService(SECRET, 60, Clock.systemUTC());

        JwtService.IssuedToken issued = jwtService.issueToken("alice");

        assertThat(jwtService.parseUsername(issued.token())).contains("alice");
    }

    @Test
    void expirationIsApproximatelyNowPlusTheConfiguredDuration() {
        Instant now = Instant.parse("2026-06-01T00:00:00Z");
        JwtService jwtService = new JwtService(SECRET, 30, Clock.fixed(now, ZoneOffset.UTC));

        JwtService.IssuedToken issued = jwtService.issueToken("alice");

        assertThat(issued.expiresAt()).isEqualTo(now.plusSeconds(30 * 60));
    }

    @Test
    void garbageTokenIsRejected() {
        JwtService jwtService = new JwtService(SECRET, 60, Clock.systemUTC());
        assertThat(jwtService.parseUsername("not-a-real-jwt")).isEmpty();
    }

    @Test
    void tokenSignedWithADifferentSecretIsRejected() {
        JwtService issuer = new JwtService(OTHER_SECRET, 60, Clock.systemUTC());
        JwtService verifier = new JwtService(SECRET, 60, Clock.systemUTC());

        JwtService.IssuedToken issued = issuer.issueToken("alice");

        assertThat(verifier.parseUsername(issued.token())).isEmpty();
    }

    @Test
    void expiredTokenIsRejected() {
        // Issued with a clock far in the past and a 1-minute expiration --
        // by the time this test actually runs, real wall-clock time is well
        // past that, so jjwt's (real-time) expiration check rejects it.
        Instant longAgo = Instant.parse("2020-01-01T00:00:00Z");
        JwtService jwtService = new JwtService(SECRET, 1, Clock.fixed(longAgo, ZoneOffset.UTC));

        JwtService.IssuedToken issued = jwtService.issueToken("alice");

        assertThat(jwtService.parseUsername(issued.token())).isEmpty();
    }

    @Test
    void rejectsASecretShorterThan32Bytes() {
        assertThatThrownBy(() -> new JwtService("too-short", 60, Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }
}
