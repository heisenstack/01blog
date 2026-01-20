package com.zerooneblog.api;

import com.zerooneblog.api.domain.Role;
import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.infrastructure.persistence.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.time.Instant;

// Initialize default admin user on application startup
@Configuration
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        
    }

    // Create default admin user if it doesn't exist
    @Bean
    @Transactional
    public CommandLineRunner initDatabase() {
        return args -> {

            // Check if admin user already exists
            if (userRepository.findByUsername("admin").isEmpty()) {

                // Create new admin user with both USER and ADMIN roles
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setEmail("admin@gmail.com");
                adminUser.setName("01blog Admin");
                // Encode password using BCrypt
                adminUser.setPassword(passwordEncoder.encode("Admin123"));
                
                // Assign both USER and ADMIN roles
                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(Role.USER);
                adminRoles.add(Role.ADMIN);
                adminUser.setRoles(adminRoles);
                
                // Initialize user properties
                adminUser.setEnabled(true);
                adminUser.setReportedCount(0L);
                adminUser.setReportingCount(0L);
                adminUser.setCreatedAt(Instant.now());
                
                userRepository.save(adminUser);
                System.out.println("Admin user initialized successfully.");
            } else {
                System.out.println("Admin user already exists. Skipping initialization.");
            }
        };
    }
}