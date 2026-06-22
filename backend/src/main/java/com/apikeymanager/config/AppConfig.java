package com.apikeymanager.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Used only for admin account passwords -- API keys are hashed
        // separately with SHA-256, not bcrypt. See KeyGenerationService and
        // docs/SECURITY.md for why those two cases are different.
        return new BCryptPasswordEncoder();
    }
}
