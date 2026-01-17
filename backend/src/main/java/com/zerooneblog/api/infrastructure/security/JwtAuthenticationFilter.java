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

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
                System.out.println("JwtAuthenticationFilter");
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                boolean isValid = false;
                try {
                    isValid = tokenProvider.validateToken(jwt);
                } catch (Exception ex) {
                    logger.debug("Invalid or malformed JWT token: " + ex.getMessage());
                    filterChain.doFilter(request, response);
                    return;
                }

                if (isValid) {
                    String username = tokenProvider.getUsernameFromJWT(jwt);
                    Long userIdFromToken = tokenProvider.getUserIdFromJWT(jwt);

                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        
                        if (userDetails instanceof CustomUserDetails) {
                            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
                            Long currentUserId = customUserDetails.getUserId();
                            
                            if (!currentUserId.equals(userIdFromToken)) {
                                logger.warn("User ID mismatch in token. Token userId: " + userIdFromToken + 
                                          ", Actual userId: " + currentUserId);
                                filterChain.doFilter(request, response);
                                return;
                            }

                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        } else {
                            logger.error("UserDetails is not an instance of CustomUserDetails");
                        }
                        
                    } catch (UsernameNotFoundException ex) {
                        logger.debug("User not found: " + username);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Unexpected error in JWT authentication filter", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}