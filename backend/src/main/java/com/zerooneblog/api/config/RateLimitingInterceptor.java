package com.zerooneblog.api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final int ACTION_LIMIT = 100;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        String method = request.getMethod();
        
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String identifier = getIdentifier(request);
        if (identifier == null) {
            return true;
        }
        
        Bucket bucket = buckets.computeIfAbsent(identifier, this::createBucket);

        if (bucket.tryConsume(1)) {
            return true;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"error\": \"Too Many Requests\", \"message\": \"Slow down! Please wait a minute.\"}");
        return false;
    }

    private Bucket createBucket(String identifier) {
        // 100 tokens capacity, refilled at rate of 100 tokens per minute
        Bandwidth limit = Bandwidth.builder()
                .capacity(ACTION_LIMIT)
                .refillIntervally(ACTION_LIMIT, Duration.ofMinutes(1))
                .build();
                
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String getIdentifier(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }

        return request.getRemoteAddr();
    }
}