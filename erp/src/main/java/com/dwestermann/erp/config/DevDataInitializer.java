package com.dwestermann.erp.config;

import com.dwestermann.erp.security.entity.Role;
import com.dwestermann.erp.security.entity.User;
import com.dwestermann.erp.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "default"}) // Nur für Development-Profile
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("🚀 Initializing development data...");

        createDefaultUsers();

        log.info("✅ Development data initialization completed");
    }

    private void createDefaultUsers() {
        final String TENANT_ID = "dev-tenant";

        // Admin User für Development
        if (userRepository.findByEmail("admin@erp.dev").isEmpty()) {
            User adminUser = new User();
            adminUser.setEmail("admin@erp.dev");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setRole(Role.SUPER_ADMIN);
            adminUser.setTenantId(TENANT_ID);
            adminUser.setAccountLocked(false);
            adminUser.setFailedLoginAttempts(0);

            userRepository.save(adminUser);
            log.info("✅ Created admin user: admin@erp.dev / admin123");
        } else {
            log.info("ℹ️  Admin user already exists");
        }

        // Standard User für Tests
        if (userRepository.findByEmail("user@erp.dev").isEmpty()) {
            User standardUser = new User();
            standardUser.setEmail("user@erp.dev");
            standardUser.setPassword(passwordEncoder.encode("user123"));
            standardUser.setFirstName("Test");
            standardUser.setLastName("User");
            standardUser.setRole(Role.USER);
            standardUser.setTenantId(TENANT_ID);
            standardUser.setAccountLocked(false);
            standardUser.setFailedLoginAttempts(0);

            userRepository.save(standardUser);
            log.info("✅ Created standard user: user@erp.dev / user123");
        } else {
            log.info("ℹ️  Standard user already exists");
        }

        // Manager User für Tests
        if (userRepository.findByEmail("manager@erp.dev").isEmpty()) {
            User managerUser = new User();
            managerUser.setEmail("manager@erp.dev");
            managerUser.setPassword(passwordEncoder.encode("manager123"));
            managerUser.setFirstName("Manager");
            managerUser.setLastName("User");
            managerUser.setRole(Role.MANAGER);
            managerUser.setTenantId(TENANT_ID);
            managerUser.setAccountLocked(false);
            managerUser.setFailedLoginAttempts(0);

            userRepository.save(managerUser);
            log.info("✅ Created manager user: manager@erp.dev / manager123");
        } else {
            log.info("ℹ️  Manager user already exists");
        }

        log.info("📊 Total users in dev-tenant: {}", userRepository.countByTenantId(TENANT_ID));
    }
}