package com.bookreview.controller;

import com.bookreview.dto.AuthResponse;
import com.bookreview.dto.LoginRequest;
import com.bookreview.dto.SignupRequest;
import com.bookreview.model.Role;
import com.bookreview.model.User;
import com.bookreview.repository.UserRepository;
import com.bookreview.security.JwtService;
import com.bookreview.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setName("Test User");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setRoles(new HashSet<>());
    }

    @Test
    void signup_shouldCreateUserAndReturnToken_whenEmailNotExists() {
        // Given
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(anyString(), any(Map.class))).thenReturn("jwt-token");

        // When
        ResponseEntity<?> response = authController.signup(signupRequest);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(AuthResponse.class);
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertThat(authResponse.getToken()).isEqualTo("jwt-token");

        verify(userRepository).existsByEmail(signupRequest.getEmail());
        verify(passwordEncoder).encode(signupRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(user.getEmail(), Map.of("roles", java.util.List.of("USER")));
        verify(emailService).sendSignupEmail(user.getEmail(), user.getName());
    }

    @Test
    void signup_shouldReturnBadRequest_whenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        // When
        ResponseEntity<?> response = authController.signup(signupRequest);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertThat(responseBody.get("error")).isEqualTo("Email already registered");

        verify(userRepository).existsByEmail(signupRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendSignupEmail(anyString(), anyString());
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn(loginRequest.getEmail());
        when(jwtService.generateToken(loginRequest.getEmail(), Map.of())).thenReturn("jwt-token");

        // When
        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo("jwt-token");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(loginRequest.getEmail(), Map.of());
    }

    @Test
    void logout_shouldReturnSuccessMessage() {
        // When
        ResponseEntity<?> response = authController.logout();

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertThat(responseBody.get("message")).isEqualTo("Logged out");
    }
}
