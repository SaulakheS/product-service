package com.zestindia.productservice;

import com.zestindia.productservice.entity.ERole;
import com.zestindia.productservice.entity.Role;
import com.zestindia.productservice.entity.User;
import com.zestindia.productservice.repository.RoleRepository;
import com.zestindia.productservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@SpringBootApplication
public class ProductServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDefaultData(RoleRepository roleRepository,
                                            UserRepository userRepository,
                                            PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("Checking and initializing default roles and users...");

            // Initialize Roles
            Role roleUser = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_USER)));

            Role roleAdmin = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_ADMIN)));

            // Seed default Admin user if not present
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User("admin", "admin@zestindia.com", passwordEncoder.encode("Admin@123"));
                admin.setRoles(Set.of(roleAdmin, roleUser));
                userRepository.save(admin);
                log.info("Default ADMIN user seeded (username: 'admin', password: 'Admin@123')");
            }

            // Seed default Normal user if not present
            if (!userRepository.existsByUsername("user")) {
                User standardUser = new User("user", "user@zestindia.com", passwordEncoder.encode("User@123"));
                standardUser.setRoles(Set.of(roleUser));
                userRepository.save(standardUser);
                log.info("Default USER seeded (username: 'user', password: 'User@123')");
            }
        };
    }
}
