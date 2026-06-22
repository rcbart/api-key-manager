package com.apikeymanager.config;

import com.apikeymanager.domain.AdminUser;
import com.apikeymanager.repository.AdminUserRepository;
import java.time.Clock;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the very first admin account from ADMIN_USERNAME/ADMIN_PASSWORD,
 * but only if the admin_users table is completely empty -- there is no
 * public self-registration endpoint, by design (see docs/SECURITY.md).
 * After the first admin exists, these env vars are ignored; create
 * additional admins by inserting directly into admin_users with a bcrypt
 * hash, or extend the app with an admin-management endpoint if you need
 * more than one operator regularly.
 */
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final String bootstrapUsername;
    private final String bootstrapPassword;

    public AdminBootstrapRunner(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            Clock clock,
            @Value("${admin.bootstrap.username:}") String bootstrapUsername,
            @Value("${admin.bootstrap.password:}") String bootstrapPassword) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.bootstrapUsername = bootstrapUsername;
        this.bootstrapPassword = bootstrapPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminUserRepository.count() > 0) {
            return;
        }

        if (bootstrapUsername.isBlank() || bootstrapPassword.isBlank()) {
            log.warn(
                    "No admin accounts exist and ADMIN_USERNAME/ADMIN_PASSWORD are not set -- nobody can log in. "
                            + "Set both env vars and restart, or insert an admin_users row directly. "
                            + "See docs/SETUP_GUIDE.md.");
            return;
        }

        AdminUser admin = new AdminUser(
                UUID.randomUUID(), bootstrapUsername, passwordEncoder.encode(bootstrapPassword), clock.instant());
        adminUserRepository.save(admin);
        log.info("Bootstrapped initial admin account username={}", bootstrapUsername);
    }
}
