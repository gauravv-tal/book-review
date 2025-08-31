package com.bookreview.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String testSecret = Base64.getEncoder().encodeToString("mySecretKeyForTestingPurposesOnly123456789".getBytes());
    private final long testExpirationMillis = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", testSecret);
        ReflectionTestUtils.setField(jwtService, "expirationMillis", testExpirationMillis);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        // Arrange
        String username = "test@example.com";
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "USER");

        // Act
        String token = jwtService.generateToken(username, extraClaims);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        // Arrange
        String username = "test@example.com";
        String token = jwtService.generateToken(username, new HashMap<>());

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    void extractClaim_shouldReturnCorrectClaim() {
        // Arrange
        String username = "test@example.com";
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "USER");
        String token = jwtService.generateToken(username, extraClaims);

        // Act
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

        // Assert
        assertEquals("USER", role);
        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(new Date()) || issuedAt.equals(new Date()));
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        // Arrange
        String username = "test@example.com";
        String token = jwtService.generateToken(username, new HashMap<>());

        // Act
        boolean isValid = jwtService.isTokenValid(token, username);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_shouldReturnFalseForWrongUsername() {
        // Arrange
        String username = "test@example.com";
        String wrongUsername = "wrong@example.com";
        String token = jwtService.generateToken(username, new HashMap<>());

        // Act
        boolean isValid = jwtService.isTokenValid(token, wrongUsername);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() {
        // Arrange
        String username = "test@example.com";
        
        // Create a service with very short expiration
        JwtService shortExpiryService = new JwtService();
        ReflectionTestUtils.setField(shortExpiryService, "secret", testSecret);
        ReflectionTestUtils.setField(shortExpiryService, "expirationMillis", 1L); // 1 millisecond
        
        String token = shortExpiryService.generateToken(username, new HashMap<>());
        
        // Wait for token to expire
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThrows(ExpiredJwtException.class, ()-> {
            shortExpiryService.isTokenValid(token, username);
        });

    }

    @Test
    void extractUsername_shouldThrowExceptionForInvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> {
            jwtService.extractUsername(invalidToken);
        });
    }

    @Test
    void extractUsername_shouldThrowExceptionForMalformedToken() {
        // Arrange
        String malformedToken = "not.a.jwt";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtService.extractUsername(malformedToken);
        });
    }

    @Test
    void generateToken_shouldIncludeExtraClaims() {
        // Arrange
        String username = "test@example.com";
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("userId", 123L);

        // Act
        String token = jwtService.generateToken(username, extraClaims);

        // Assert
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        Long userId = jwtService.extractClaim(token, claims -> claims.get("userId", Long.class));
        
        assertEquals("ADMIN", role);
        assertEquals(123L, userId);
    }

    @Test
    void generateToken_shouldSetCorrectExpiration() {
        // Arrange
        String username = "test@example.com";
        Date beforeGeneration = new Date();

        // Act
        String token = jwtService.generateToken(username, new HashMap<>());
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        // Assert
        Date expectedExpiration = new Date(beforeGeneration.getTime() + testExpirationMillis);
        long timeDifference = Math.abs(expiration.getTime() - expectedExpiration.getTime());
        assertTrue(timeDifference < 1000); // Allow 1 second difference for test execution time
    }

    @Test
    void extractClaim_shouldHandleNullClaims() {
        // Arrange
        String username = "test@example.com";
        String token = jwtService.generateToken(username, new HashMap<>());

        // Act
        String nonExistentClaim = jwtService.extractClaim(token, claims -> claims.get("nonExistent", String.class));

        // Assert
        assertNull(nonExistentClaim);
    }
}
