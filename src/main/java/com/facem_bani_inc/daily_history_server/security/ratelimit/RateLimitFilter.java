package com.facem_bani_inc.daily_history_server.security.ratelimit;

import com.facem_bani_inc.daily_history_server.model.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitFilter extends OncePerRequestFilter {

    private record Rule(String method, String path, int maxRequests, Duration window) {
    }

    private record Counter(AtomicInteger count, Duration window) {
    }

    private static final List<Rule> RULES = List.of(
            new Rule("POST", "/api/v1/auth/signin", 10, Duration.ofMinutes(1)),
            new Rule("POST", "/api/v1/auth/signin", 50, Duration.ofHours(1)),
            new Rule("POST", "/api/v1/auth/register", 3, Duration.ofHours(1)),
            new Rule("POST", "/api/v1/support", 5, Duration.ofHours(1))
    );

    private final Cache<String, Counter> counters = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfter(new Expiry<String, Counter>() {
                @Override
                public long expireAfterCreate(String key, Counter counter, long currentTime) {
                    return counter.window().toNanos();
                }

                @Override
                public long expireAfterUpdate(String key, Counter counter, long currentTime, long currentDuration) {
                    return currentDuration;
                }

                @Override
                public long expireAfterRead(String key, Counter counter, long currentTime, long currentDuration) {
                    return currentDuration;
                }
            })
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ip = clientIp(request);
        boolean limited = false;
        for (Rule rule : matchRules(request)) {
            String key = rule.path() + ":" + rule.window().toSeconds() + ":" + ip;
            int count = counters.get(key, k -> new Counter(new AtomicInteger(0), rule.window()))
                    .count().incrementAndGet();
            if (count > rule.maxRequests()) {
                limited = true;
            }
        }

        if (limited) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(
                    new ApiResponse(false, "Too many requests. Please try again later.")));
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return matchRules(request).isEmpty();
    }

    private static List<Rule> matchRules(HttpServletRequest request) {
        return RULES.stream()
                .filter(rule -> rule.method().equals(request.getMethod()) && rule.path().equals(request.getRequestURI()))
                .toList();
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] hops = forwarded.split(",");
            return hops[hops.length - 1].trim();
        }
        return request.getRemoteAddr();
    }
}
