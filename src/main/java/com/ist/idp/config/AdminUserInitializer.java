package com.ist.idp.config;

import com.ist.idp.enums.Role;
import com.ist.idp.model.User;
import com.ist.idp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AdminUserInitializer {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Bean
    @Profile("!test") // Don't run this in tests
    public CommandLineRunner initAdminUser() {
        return args -> {
            String adminEmail = "admin@example.com";
            String adminPassword = "admin";
            
            // Check if admin user already exists
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User adminUser = User.builder()
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .role(Role.ADMIN)
                        .emailVerified(true)
                        .build();
                
                userRepository.save(adminUser);
                log.info("Created default admin user with email: {}", adminEmail);
            } else {
                log.info("Admin user already exists, skipping creation");
            }
        };
    }
}
