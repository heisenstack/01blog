package com.zerooneblog.api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

// Rate limiting to prevent abuse on POST, PUT, DELETE requests
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final int ACTION_LIMIT = 100;
    // Store rate limiter per user
    private final Map<String, SimpleRateLimiter> limiters = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        String method = request.getMethod();
        
        // Skip rate limiting for GET and OPTIONS requests
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String identifier = getIdentifier(request);
        if (identifier == null) {
            return true;
        }
        
        // Create or get existing limiter for user
        SimpleRateLimiter limiter = limiters.computeIfAbsent(identifier, k -> new SimpleRateLimiter(ACTION_LIMIT));

        // Check if user exceeded rate limit
        if (limiter.isAllowed()) {
            return true;
        }

        // Return 429 Too Many Requests
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"error\": \"Too Many Requests\", \"message\": \"Slow down! Please wait a minute.\"}");
        return false;
    }

    // Get authenticated user identifier or IP address
    private String getIdentifier(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }

        return request.getRemoteAddr();
    }

    // Simple sliding window rate limiter
    private static class SimpleRateLimiter {
        private final int limit;
        private int counter;
        private long windowStartTime;
        private static final long WINDOW_SIZE_MS = TimeUnit.MINUTES.toMillis(1);

        public SimpleRateLimiter(int limit) {
            this.limit = limit;
            this.counter = 0;
            this.windowStartTime = System.currentTimeMillis();
        }

        // Thread-safe check and increment counter
        public synchronized boolean isAllowed() {
            long currentTime = System.currentTimeMillis();
            
            // Reset counter if time window expired
            if (currentTime - windowStartTime > WINDOW_SIZE_MS) {
                windowStartTime = currentTime;
                counter = 0;
            }

            // Allow if under limit
            if (counter < limit) {
                counter++;
                return true;
            }
            return false;
        }
    }
}