package com.zerooneblog.api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final int ACTION_LIMIT = 100;
    private final Map<String, SimpleRateLimiter> limiters = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {
        System.out.println("RateLimitingInterceptor");

        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String identifier = getIdentifier(request);
        if (identifier == null) {
            return true;
        }
        SimpleRateLimiter limiter = limiters.computeIfAbsent(identifier, k -> new SimpleRateLimiter(ACTION_LIMIT));

        if (limiter.isAllowed()) {
            return true;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"error\": \"Too Many Requests\", \"message\": \"Slow down! Please wait a minute.\"}");
        return false;
    }

    private String getIdentifier(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }

        return null;
    }

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

        public synchronized boolean isAllowed() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - windowStartTime > WINDOW_SIZE_MS) {
                windowStartTime = currentTime;
                counter = 0;
            }

            if (counter < limit) {
                counter++;
                return true;
            }
            return false;
        }
    }
}