package com.facem_bani_inc.daily_history_server.controller;

import com.facem_bani_inc.daily_history_server.entity.Role;
import com.facem_bani_inc.daily_history_server.entity.User;
import com.facem_bani_inc.daily_history_server.model.enums.EAuthProvider;
import com.facem_bani_inc.daily_history_server.model.enums.ERole;
import com.facem_bani_inc.daily_history_server.payload.request.GoogleSignInRequest;
import com.facem_bani_inc.daily_history_server.payload.request.LoginRequest;
import com.facem_bani_inc.daily_history_server.payload.request.SignupRequest;
import com.facem_bani_inc.daily_history_server.payload.response.JwtResponse;
import com.facem_bani_inc.daily_history_server.payload.response.MessageResponse;
import com.facem_bani_inc.daily_history_server.repository.RoleRepository;
import com.facem_bani_inc.daily_history_server.repository.UserRepository;
import com.facem_bani_inc.daily_history_server.security.jwt.JwtUtils;
import com.facem_bani_inc.daily_history_server.security.service.GoogleAuthService;
import com.facem_bani_inc.daily_history_server.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final GoogleAuthService googleAuthService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getAvatarUrl(),
                roles
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }
        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword())
        );
        user.setAuthProvider(EAuthProvider.LOCAL);
        user.setAvatarUrl("https://ui-avatars.com/api/?name=" + user.getUsername() + "&background=ffd700");
        Role userRole = roleRepository.findByName(ERole.USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogle(@Valid @RequestBody GoogleSignInRequest request) {
        return ResponseEntity.ok(googleAuthService.authenticate(request.getIdToken()));
    }
}
