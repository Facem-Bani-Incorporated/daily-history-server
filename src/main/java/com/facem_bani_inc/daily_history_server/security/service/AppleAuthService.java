package com.facem_bani_inc.daily_history_server.security.service;

import com.facem_bani_inc.daily_history_server.entity.Role;
import com.facem_bani_inc.daily_history_server.entity.User;
import com.facem_bani_inc.daily_history_server.model.enums.EAuthProvider;
import com.facem_bani_inc.daily_history_server.model.enums.ERole;
import com.facem_bani_inc.daily_history_server.payload.response.JwtResponse;
import com.facem_bani_inc.daily_history_server.repository.RoleRepository;
import com.facem_bani_inc.daily_history_server.repository.UserRepository;
import com.facem_bani_inc.daily_history_server.security.jwt.JwtUtils;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppleAuthService {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    private final JWKSource<SecurityContext> appleJwkSource;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;

    @Value("${apple.bundle-id}")
    private String bundleId;

    public JwtResponse authenticate(String idTokenString, String fullName, String email) {
        JWTClaimsSet claims = verifyToken(idTokenString);
        String sub = claims.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple token has no subject");
        }
        String tokenEmail = getStringClaim(claims, "email");
        Boolean emailVerified = getBooleanClaim(claims, "email_verified");
        if (emailVerified != null && !emailVerified) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple email is not verified");
        }
        String effectiveEmail = (email != null && !email.isBlank()) ? email : tokenEmail;
        User user = userRepository.findByAuthProviderAndProviderUserId(EAuthProvider.APPLE, sub)
                .orElseGet(() -> createUserFromApple(sub, effectiveEmail, fullName));
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getAvatarUrl(),
                userDetails.isPro(),
                roles
        );
    }

    private JWTClaimsSet verifyToken(String idTokenString) {
        try {
            ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
            processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, appleJwkSource));
            JWTClaimsSet claims = processor.process(idTokenString, null);
            String issuer = claims.getIssuer();
            if (!APPLE_ISSUER.equals(issuer)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token issuer");
            }
            List<String> audience = claims.getAudience();
            if (audience == null || !audience.contains(bundleId)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token audience");
            }
            return claims;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple token verification failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple token verification failed");
        }
    }

    private User createUserFromApple(String sub, String email, String fullName) {
        String baseUsername;
        if (fullName != null && !fullName.isBlank()) {
            baseUsername = fullName.replaceAll("[^a-zA-Z0-9._-]", "").toLowerCase();
            if (baseUsername.isBlank()) baseUsername = "user";
        } else if (email != null && !email.isBlank()) {
            baseUsername = deriveUsername(email);
        } else {
            baseUsername = "user_" + sub.substring(0, Math.min(6, sub.length()));
        }
        String username = ensureUniqueUsername(baseUsername);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(null);
        user.setAuthProvider(EAuthProvider.APPLE);
        user.setProviderUserId(sub);
        user.setAvatarUrl("https://ui-avatars.com/api/?name=" + username + "&background=ffd700");

        Role userRole = roleRepository.findByName(ERole.USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    private String deriveUsername(String email) {
        String localPart = email.split("@", 2)[0];
        String cleaned = localPart.replaceAll("[^a-zA-Z0-9._-]", "");
        if (cleaned.isBlank()) return "user";
        return cleaned;
    }

    private String ensureUniqueUsername(String base) {
        String candidate = base;
        int i = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + i;
            i++;
        }
        return candidate;
    }

    private String getStringClaim(JWTClaimsSet claims, String name) {
        try {
            return claims.getStringClaim(name);
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean getBooleanClaim(JWTClaimsSet claims, String name) {
        try {
            return claims.getBooleanClaim(name);
        } catch (Exception e) {
            return null;
        }
    }
}
