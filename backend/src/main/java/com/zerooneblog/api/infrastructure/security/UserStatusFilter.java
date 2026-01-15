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

@Component
public class UserStatusFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserStatusFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String username = auth.getName();
            
            User user = userRepository.findByUsername(username).orElse(null);
            
            // deleted user
            // if (user == null) {
            //     SecurityContextHolder.clearContext();
                
            //     response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            //     response.setContentType("application/json");
                
            //     MessageResponse errorResponse = new MessageResponse(
            //         "USER_DELETED",
            //         "Your account has been deleted. Please contact support if you believe this is an error."
            //     );
                
            //     objectMapper.writeValue(response.getOutputStream(), errorResponse);
            //     return;
            // }
            
            // banned user
            if (!user.isEnabled()) {
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
        
        filterChain.doFilter(request, response);
    }
}