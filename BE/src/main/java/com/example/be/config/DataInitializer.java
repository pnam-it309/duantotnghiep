package com.example.be.config;

import com.example.be.entity.User;
import com.example.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Seed Admin
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("123"))
                    .email("admin@example.com")
                    .fullName("System Admin")
                    .role("ADMIN")
                    .membershipTier("DIAMOND")
                    .build();
            userRepository.save(admin);
            System.out.println("Seeded admin account: admin/123");
        }

        // Seed Customer
        if (userRepository.findByUsername("customer").isEmpty()) {
            User customer = User.builder()
                    .username("customer")
                    .password(passwordEncoder.encode("123"))
                    .email("customer@example.com")
                    .fullName("Regular Customer")
                    .role("USER")
                    .membershipTier("SILVER")
                    .build();
            userRepository.save(customer);
            System.out.println("Seeded customer account: customer/123");
        }
    }
}
