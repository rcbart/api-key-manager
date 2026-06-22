package com.apikeymanager.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class KeyGenerationServiceTest {

    private final KeyGenerationService service = new KeyGenerationService("ak_live_", 6);

    @Test
    void generatesAKeyWithTheConfiguredPrefix() {
        KeyGenerationService.GeneratedKey generated = service.generate();
        assertThat(generated.rawKey()).startsWith("ak_live_");
    }

    @Test
    void displayPrefixIsThePrefixPlusTheConfiguredNumberOfRandomCharacters() {
        KeyGenerationService.GeneratedKey generated = service.generate();
        // "ak_live_" (8 chars) + 6 display chars = 14
        assertThat(generated.displayPrefix()).hasSize(14);
        assertThat(generated.rawKey()).startsWith(generated.displayPrefix());
    }

    @Test
    void hashOfTheRawKeyMatchesTheGeneratedHash() {
        KeyGenerationService.GeneratedKey generated = service.generate();
        assertThat(service.hash(generated.rawKey())).isEqualTo(generated.keyHash());
    }

    @Test
    void hashIsADeterministicSha256HexDigest() {
        // Known SHA-256("hello") test vector.
        String expected = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";
        assertThat(service.hash("hello")).isEqualTo(expected);
    }

    @Test
    void hashingTheSameInputTwiceProducesTheSameOutput() {
        String hash1 = service.hash("some-raw-key-value");
        String hash2 = service.hash("some-raw-key-value");
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void differentInputsProduceDifferentHashes() {
        assertThat(service.hash("key-a")).isNotEqualTo(service.hash("key-b"));
    }

    @Test
    void generatedKeysAreUnique() {
        Set<String> keys = new HashSet<>();
        IntStream.range(0, 500).forEach(i -> keys.add(service.generate().rawKey()));
        assertThat(keys).hasSize(500);
    }

    @Test
    void rawKeyIsNotTooShortToBeGuessable() {
        // prefix + 43 url-safe base64 chars (32 random bytes, no padding)
        KeyGenerationService.GeneratedKey generated = service.generate();
        assertThat(generated.rawKey().length()).isGreaterThanOrEqualTo("ak_live_".length() + 43);
    }
}
