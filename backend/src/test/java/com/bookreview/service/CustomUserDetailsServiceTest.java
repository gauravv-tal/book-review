package com.bookreview.service;

import com.bookreview.model.User;
import com.bookreview.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {
    private UserRepository userRepository;
    private CustomUserDetailsService service;

    @BeforeEach
    void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        service = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_mapsUserAndRoles() {
        User u = new User();
        u.setEmail("jane@example.com");
        u.setPasswordHash("hash");
        // If roles is null-safe in entity, this can be empty. Set via reflection if needed.
        u.setRoles(Set.of(com.bookreview.model.Role.USER));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(u));

        UserDetails ud = service.loadUserByUsername("jane@example.com");
        assertEquals("jane@example.com", ud.getUsername());
        assertEquals("hash", ud.getPassword());
        assertTrue(ud.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_notFound_throws() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing@example.com"));
    }
}
