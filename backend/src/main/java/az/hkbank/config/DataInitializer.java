package az.hkbank.config;

import az.hkbank.module.user.entity.Role;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default admin user on startup when none exists for the configured email.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin already exists, skipping...");
            return;
        }

        User admin = User.builder()
                .firstName("Admin")
                .lastName("System")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .phoneNumber("+994500000000")
                .role(Role.ADMIN)
                .isDeleted(false)
                .build();

        userRepository.save(admin);
        log.info("✅ Default admin created: {}", adminEmail);
    }
}
