package com.zerooneblog.api.infrastructure.security;

import com.zerooneblog.api.domain.Role;
import com.zerooneblog.api.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

// Custom implementation of Spring Security UserDetails for JWT authentication
public class CustomUserDetails implements UserDetails {
    
    private final User user;
    
    public CustomUserDetails(User user) {
        this.user = user;
    }
    
    // Get the wrapped user entity
    public User getUser() {
        return user;
    }
    
    // Get user ID for token validation
    public Long getUserId() {
        return user.getId();
    }
    
    // Convert user roles to Spring Security authorities
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = user.getRoles();
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
    // Account status checks
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    // Check if user is enabled (not banned)
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}