package com.bookreview.controller;

import com.bookreview.dto.AuthResponse;
import com.bookreview.dto.LoginRequest;
import com.bookreview.dto.SignupRequest;
import com.bookreview.model.Role;
import com.bookreview.model.User;
import com.bookreview.repository.UserRepository;
import com.bookreview.security.JwtService;
import com.bookreview.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(new HashSet<>(Collections.singleton(Role.USER)));
        userRepository.save(user);
        // Send welcome email (no-op if disabled)
        emailService.sendSignupEmail(user.getEmail(), user.getName());
        // Auto-login: issue token
        String token = jwtService.generateToken(user.getEmail(), Map.of("roles", List.of("USER")));
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String username = auth.getName();
        String token = jwtService.generateToken(username, Map.of());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Stateless JWT: client discards token
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
