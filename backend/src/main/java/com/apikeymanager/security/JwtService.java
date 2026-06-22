package com.apikeymanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Issues and validates the JWTs used for admin session authentication. */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String INSECURE_DEFAULT_SECRET = "CHANGE_ME_CHANGE_ME_CHANGE_ME_32_CHARS_MIN";
    private static final int MIN_SECRET_BYTES = 32; // 256 bits, required for HS256

    private final SecretKey signingKey;
    private final Duration expiration;
    private final Clock clock;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-minutes}") long expirationMinutes,
            Clock clock) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret (JWT_SECRET) must be at least " + MIN_SECRET_BYTES + " bytes long for HS256");
        }
        if (INSECURE_DEFAULT_SECRET.equals(secret)) {
            log.warn(
                    "JWT_SECRET is using the insecure default placeholder. This is fine for a first local run, "
                            + "but every issued token is forgeable by anyone who reads this codebase. Set a real "
                            + "random JWT_SECRET before deploying anywhere -- see docs/SECURITY.md.");
        }

        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.expiration = Duration.ofMinutes(expirationMinutes);
        this.clock = clock;
    }

    public record IssuedToken(String token, Instant expiresAt) {
    }

    public IssuedToken issueToken(String username) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(expiration);

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        return new IssuedToken(token, expiresAt);
    }

    /** Returns the token's subject (username) if it's valid and not expired, else empty. */
    public Optional<String> parseUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("rejected JWT: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
