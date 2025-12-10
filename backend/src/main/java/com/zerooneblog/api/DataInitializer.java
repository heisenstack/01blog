package com.zerooneblog.api;

import com.zerooneblog.api.domain.Role;
import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.infrastructure.persistence.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Configuration
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    @Transactional
    public CommandLineRunner initDatabase() {
        return args -> {

            if (userRepository.findByUsername("admin").isEmpty()) {

                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setEmail("admin@gmail.com");
                adminUser.setName("System Admin");
                adminUser.setPassword(passwordEncoder.encode("Admin123"));               
                adminUser.setRole(Role.ADMIN); 
                adminUser.setRole(Role.USER); 
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