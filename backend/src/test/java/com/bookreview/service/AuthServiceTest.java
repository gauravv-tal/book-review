package com.bookreview.service;

import com.bookreview.model.User;
import com.bookreview.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private UserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        authService = new AuthService(userRepository);
    }

    @Test
    void extractUserId_withUserDetailsPrincipal_returnsId() {
        UserDetails ud = org.springframework.security.core.userdetails.User
                .withUsername("alice@example.com").password("x").roles("USER").build();
        Authentication auth = new TestingAuthenticationToken(ud, null, "ROLE_USER");
        auth.setAuthenticated(true);

        User user = new User();
        user.setId(42L);
        user.setEmail("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        Long id = authService.extractUserId(auth);
        assertEquals(42L, id);
    }

    @Test
    void extractUserId_withNamePrincipal_returnsId() {
        Authentication auth = new TestingAuthenticationToken("bob@example.com", null, "ROLE_USER");
        auth.setAuthenticated(true);

        User user = new User();
        user.setId(7L);
        user.setEmail("bob@example.com");
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(user));

        Long id = authService.extractUserId(auth);
        assertEquals(7L, id);
    }

    @Test
    void extractUserId_notAuthenticated_throws() {
        Authentication auth = new TestingAuthenticationToken("x", null);
        auth.setAuthenticated(false);
        assertThrows(RuntimeException.class, () -> authService.extractUserId(auth));
    }

    @Test
    void extractUserId_userNotFound_throws() {
        Authentication auth = new TestingAuthenticationToken("ghost@example.com", null);
        auth.setAuthenticated(true);
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.extractUserId(auth));
    }
}
