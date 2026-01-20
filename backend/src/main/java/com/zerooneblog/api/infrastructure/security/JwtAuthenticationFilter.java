package com.zerooneblog.api.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerooneblog.api.interfaces.dto.MessageResponse;
import java.io.IOException;

// JWT filter to validate tokens and authenticate users
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract JWT token from Authorization header
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                try {
                    // Validate token signature and expiration
                    if (!tokenProvider.validateToken(jwt)) {
                        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                        return;
                    }

                    // Extract user info from token
                    String username = tokenProvider.getUsernameFromJWT(jwt);
                    Long userIdFromToken = tokenProvider.getUserIdFromJWT(jwt);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    if (userDetails instanceof CustomUserDetails) {
                        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
                        Long currentUserId = customUserDetails.getUserId();
                        
                        // Verify user ID matches token
                        if (!currentUserId.equals(userIdFromToken)) {
                            logger.warn("User ID mismatch in token. Token userId: " + userIdFromToken + 
                                      ", Actual userId: " + currentUserId);
                            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token user ID mismatch");
                            return;
                        }

                        // Create authentication token and set in security context
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        logger.error("UserDetails is not an instance of CustomUserDetails");
                        sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid user details");
                        return;
                    }
                    
                } catch (UsernameNotFoundException ex) {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                    return;
                } catch (IllegalArgumentException ex) {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT claims string is empty");
                    return;
                } catch (Exception ex) {
                    logger.error("JWT authentication failed", ex);
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
                    return;
                }
            }
            
            // Continue filter chain
            filterChain.doFilter(request, response);
            
        } catch (Exception ex) {
            logger.error("Unexpected error in JWT authentication filter", ex);
            try {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
            } catch (IOException e) {
                logger.error("Failed to send error response", e);
            }
        }
    }

    // Extract JWT from Authorization header (Bearer token)
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Send JSON error response to client
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        
        MessageResponse errorResponse = new MessageResponse("error", message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}