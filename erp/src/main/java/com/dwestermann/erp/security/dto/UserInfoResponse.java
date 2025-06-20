// UserInfoResponse.java
package com.dwestermann.erp.security.dto;

import com.dwestermann.erp.security.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserInfoResponse {

    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String tenantId;

    private boolean isEmailVerified;
    private boolean isAccountLocked;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}