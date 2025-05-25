package com.example.FoodHub.configuration;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.enums.Role;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.repository.RoleRepository;
import com.example.FoodHub.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RoleRepository roleRepository;
    @Bean
    ApplicationRunner applicationRunner() {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .roleName(roleRepository.findById(Role.ADMIN.name())
                                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED)))
                        .email("amdin@foodhub.com") // REQUIRED if `@NotNull`
                        .status("ACTIVE")           // REQUIRED if `@NotNull`
                        .registrationDate(Instant.now()) // REQUIRED if `@NotNull`
                        .isAuthUser(false)         // Optional (default is already false)
                        .build();
                userRepository.save(user);

                log.warn("User admin has been created with default password: gm, please change its password immediately.");
            }
        };
    }
}
