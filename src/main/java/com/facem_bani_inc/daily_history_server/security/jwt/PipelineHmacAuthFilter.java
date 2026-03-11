package com.facem_bani_inc.daily_history_server.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

public class PipelineHmacAuthFilter extends OncePerRequestFilter {

    private final byte[] secret;
    private final long allowedSkewSeconds;

    public PipelineHmacAuthFilter(String secret, long allowedSkewSeconds) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.allowedSkewSeconds = allowedSkewSeconds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
        String timestampHeader = request.getHeader("X-Timestamp");
        String signatureHeader = request.getHeader("X-Signature");

        if (timestampHeader == null || signatureHeader == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampHeader);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - timestamp) > allowedSkewSeconds) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String payload = timestampHeader + "." + new String(body, StandardCharsets.UTF_8);
        String expected = hmacBase64(secret, payload);

        if (!constantTimeEquals(expected, signatureHeader)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        AbstractAuthenticationToken auth = new AbstractAuthenticationToken(List.of(new SimpleGrantedAuthority("ROLE_PIPELINE"))) {
            @Override public Object getCredentials() { return ""; }
            @Override public Object getPrincipal() { return "pipeline"; }
        };
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(new CachedBodyHttpServletRequest(request, body), response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equals(request.getMethod()) && "/api/daily-content".equals(request.getRequestURI()));
    }

    private static String hmacBase64(byte[] secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        if (x.length != y.length) return false;
        int r = 0;
        for (int i = 0; i < x.length; i++) r |= x[i] ^ y[i];
        return r == 0;
    }
}