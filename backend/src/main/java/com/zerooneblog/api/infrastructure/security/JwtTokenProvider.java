package com.zerooneblog.api.infrastructure.security;

import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.infrastructure.persistence.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private int jwtExpirationInMs;

    private final UserRepository userRepository;

    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Helper method to create the key from secret
    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Generate token for authenticated user
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found during token generation"));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(username)
                .claim("userId", user.getId()) 
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key())
                .compact();
    }

    // Extract username from token
    // public String getUsernameFromJWT(String token) {
    //     Claims claims = Jwts.parser()
    //             .verifyWith(key())
    //             .build()
    //             .parseSignedClaims(token)
    //             .getPayload();
    //     return claims.getSubject();
    // }

    // Validate token
    // public boolean validateToken(String token) {
    //     try {
    //         Jwts.parser().setSigningKey(key()).parseClaimsJws(token);
    //         return true;
    //     } catch (JwtException | IllegalArgumentException ex) {
    //         logger.error("Invalid JWT token: {}", ex.getMessage());
    //     }
    //     return false;
    // }
}
