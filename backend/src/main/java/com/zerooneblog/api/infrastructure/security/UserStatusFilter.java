package com.zerooneblog.api.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.infrastructure.persistence.UserRepository;
import com.zerooneblog.api.interfaces.dto.MessageResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Filter to check if authenticated user is banned before processing request
@Component
public class UserStatusFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserStatusFilter(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Get current authentication from security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String username = auth.getName();
            
            // Load user from database
            User user = userRepository.findByUsername(username).orElse(null);
            
            // Block access if user is banned
            if (user != null && !user.isEnabled()) {
                SecurityContextHolder.clearContext();
                
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                
                MessageResponse errorResponse = new MessageResponse(
                    "USER_BANNED",
                    "Your account has been banned. Please contact support."
                );
                
                objectMapper.writeValue(response.getOutputStream(), errorResponse);
                return;
            }
        }
        
        // Continue filter chain for non-banned users
        filterChain.doFilter(request, response);
    }
}