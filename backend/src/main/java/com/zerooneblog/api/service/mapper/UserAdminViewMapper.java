package com.zerooneblog.api.service.mapper;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.domain.Role;
import com.zerooneblog.api.interfaces.dto.UserAdminViewDto;


@Component
public class UserAdminViewMapper {

    public UserAdminViewDto toDto(User user) {
        UserAdminViewDto dto  = new UserAdminViewDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        Set<Role> roles = user.getRoles();
        dto.setRoles(roles);
        dto.setReportedCount(user.getReportedCount());
        dto.setReportingCount(user.getReportingCount());
        dto.setEnabled(user.isEnabled());
        return dto;
    }
}

// /*
//  private Long id;
//     private String username;
//     private String name;
//     private String email;
//     private Set<String> roles;
//     private Long reportsSubmitted; 
//     private boolean enabled;
//     private Long reportingCount;
//     private Long reportedCount;
// */