package com.bookreview.service;

import com.bookreview.model.User;
import com.bookreview.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Extracts the user ID from the current authentication context
     * @param auth The authentication object from the security context
     * @return The user ID of the authenticated user
     * @throws RuntimeException if user is not found or authentication is invalid
     */
    public Long extractUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String email;
        if (auth.getPrincipal() instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = auth.getName();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        
        return user.getId();
    }
}
