package com.zerooneblog.api.interfaces.dto;

import com.zerooneblog.api.domain.Role;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserAdminViewDto {
    private Long id;
    private String username;
    private String name;
    private String email;
    private Set<Role> roles;
    private Long reportsSubmitted; 
    private boolean enabled;
    private Long reportingCount;
    private Long reportedCount;
}