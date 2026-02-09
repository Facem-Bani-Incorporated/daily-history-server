package com.facem_bani_inc.daily_history_server.security.service;

import com.facem_bani_inc.daily_history_server.entity.Role;
import com.facem_bani_inc.daily_history_server.entity.User;
import com.facem_bani_inc.daily_history_server.model.EAuthProvider;
import com.facem_bani_inc.daily_history_server.model.ERole;
import com.facem_bani_inc.daily_history_server.payload.response.JwtResponse;
import com.facem_bani_inc.daily_history_server.repository.RoleRepository;
import com.facem_bani_inc.daily_history_server.repository.UserRepository;
import com.facem_bani_inc.daily_history_server.security.jwt.JwtUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final AvatarService avatarService;

    public JwtResponse authenticate(String idTokenString) {
        GoogleIdToken idToken = verifyToken(idTokenString);
        GoogleIdToken.Payload payload = idToken.getPayload();
        String issuer = payload.getIssuer();
        if (issuer == null || (!issuer.equals("accounts.google.com") && !issuer.equals("https://accounts.google.com"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token issuer");
        }
        String sub = payload.getSubject();
        String email = payload.getEmail();
        Boolean emailVerified = payload.getEmailVerified();
        if (sub == null || sub.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google token has no subject");
        }
        if (emailVerified != null && !emailVerified) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google email is not verified");
        }

        User user = userRepository.findByAuthProviderAndProviderUserId(EAuthProvider.GOOGLE, sub)
                .orElseGet(() -> createUserFromGoogle(sub, email));

        String pictureUrl = (String) payload.get("picture");
        if ((user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) && pictureUrl != null && !pictureUrl.isBlank()) {
            String uploaded = avatarService.uploadFromUrl(pictureUrl, EAuthProvider.GOOGLE, sub);
            if (uploaded != null && !uploaded.isBlank()) {
                user.setAvatarUrl(uploaded);
                userRepository.save(user);
            }
        }
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles);
    }

    private GoogleIdToken verifyToken(String idTokenString) {
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(idTokenString);
            if (idToken == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google ID token");
            }
            return idToken;
        } catch (GeneralSecurityException | IOException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google token verification failed");
        }
    }

    private User createUserFromGoogle(String sub, String email) {
        if (email == null || email.isBlank()) email = null;
        String baseUsername = (email != null) ? deriveUsername(email) : "user_" + sub.substring(0, 6);
        String username = ensureUniqueUsername(baseUsername);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(null);
        user.setAuthProvider(EAuthProvider.GOOGLE);
        user.setProviderUserId(sub);
        Role userRole = roleRepository.findByName(ERole.USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        user.setRoles(Set.of(userRole));
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
}
