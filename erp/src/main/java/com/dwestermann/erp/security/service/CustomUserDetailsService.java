package com.dwestermann.erp.security.service;

import com.dwestermann.erp.security.entity.User;
import com.dwestermann.erp.security.repository.UserRepository;
import com.dwestermann.erp.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String tenantId = TenantContext.getTenantId();

        log.debug("Loading user by email: {} for tenant: {}", email, tenantId);

        if (tenantId == null) {
            log.error("No tenant context found when loading user: {}", email);
            throw new UsernameNotFoundException("No tenant context available");
        }

        User user = userRepository.findByEmailAndTenantId(email, tenantId)
                .orElseThrow(() -> {
                    log.warn("User not found: {} in tenant: {}", email, tenantId);
                    return new UsernameNotFoundException("User not found: " + email);
                });

        log.debug("Successfully loaded user: {} with roles: {} for tenant: {}",
                email, user.getRoleNames(), tenantId);

        return user;
    }

    /**
     * Load user by email without tenant context (for system operations)
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsernameAndTenant(String email, String tenantId)
            throws UsernameNotFoundException {

        log.debug("Loading user by email: {} for specific tenant: {}", email, tenantId);

        User user = userRepository.findByEmailAndTenantId(email, tenantId)
                .orElseThrow(() -> {
                    log.warn("User not found: {} in tenant: {}", email, tenantId);
                    return new UsernameNotFoundException("User not found: " + email + " in tenant: " + tenantId);
                });

        return user;
    }
}