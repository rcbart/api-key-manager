package com.apikeymanager.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Generates raw API key strings and hashes them for storage. Kept as plain,
 * dependency-free logic (no Spring Data, no entities) so it's trivial to
 * unit test in isolation -- this is the single most security-sensitive
 * class in the project.
 *
 * Format: {@code <prefix><43 url-safe base64 chars>}, e.g.
 * {@code ak_live_kJ8xQ2N7f...}. The random portion encodes 32 bytes (256
 * bits) from {@link SecureRandom} -- far more entropy than is practically
 * brute-forceable, which is what justifies hashing it with a fast,
 * non-salted SHA-256 instead of a deliberately slow algorithm like bcrypt
 * (see docs/SECURITY.md).
 */
@Service
public class KeyGenerationService {

    private static final int RANDOM_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final String prefix;
    private final int displaySuffixLength;

    public KeyGenerationService(
            @Value("${apikey.prefix}") String prefix,
            @Value("${apikey.display-suffix-length}") int displaySuffixLength) {
        this.prefix = prefix;
        this.displaySuffixLength = displaySuffixLength;
    }

    public record GeneratedKey(String rawKey, String displayPrefix, String keyHash) {
    }

    public GeneratedKey generate() {
        byte[] randomBytes = new byte[RANDOM_BYTES];
        secureRandom.nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        String rawKey = prefix + randomPart;
        String displayPrefix = buildDisplayPrefix(randomPart);
        String keyHash = sha256Hex(rawKey);

        return new GeneratedKey(rawKey, displayPrefix, keyHash);
    }

    public String hash(String rawKey) {
        return sha256Hex(rawKey);
    }

    private String buildDisplayPrefix(String randomPart) {
        int len = Math.min(displaySuffixLength, randomPart.length());
        return prefix + randomPart.substring(0, len);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by every JDK implementation -- this is
            // unreachable in practice. Fail loudly if it ever isn't.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
