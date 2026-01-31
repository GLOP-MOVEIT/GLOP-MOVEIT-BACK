package com.moveit.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private static final String SECRET_KEY = "bW92ZWl0U2VjcmV0S2V5Rm9ySlNPTldlYlRva2VuU2lnbmluZ0FuZEF1dGhlbnRpY2F0aW9u";
    private static final long JWT_EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET_KEY, JWT_EXPIRATION);
        userDetails = User.builder()
                .username("testuser@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Generate token should create valid JWT")
    void testGenerateToken_Success() {
        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // JWT should have 3 parts (header.payload.signature)
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts");
    }

    @Test
    @DisplayName("Extract username should return correct username")
    void testExtractUsername_Success() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("testuser@example.com", username);
    }

    @Test
    @DisplayName("Is token valid should return true for valid token")
    void testIsTokenValid_ValidToken_ReturnsTrue() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Is token valid should return false for wrong user")
    void testIsTokenValid_WrongUser_ReturnsFalse() {
        // Arrange
        String token = jwtService.generateToken(userDetails);
        
        UserDetails differentUser = User.builder()
                .username("different@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        // Act
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Is token valid should return false for expired token")
    void testIsTokenValid_ExpiredToken_ReturnsFalse() {
        // Arrange
        JwtService shortLivedJwtService = new JwtService(SECRET_KEY, -1000);
        String expiredToken = shortLivedJwtService.generateToken(userDetails);

        // Wait a bit to ensure token is expired
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isValid = jwtService.isTokenValid(expiredToken, userDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Extract username should throw exception for invalid token")
    void testExtractUsername_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            jwtService.extractUsername(invalidToken);
        });
    }

    @Test
    @DisplayName("Extract username should throw exception for expired token")
    void testExtractUsername_ExpiredToken_ThrowsException() {
        // Arrange
        JwtService shortLivedJwtService = new JwtService(SECRET_KEY, -1000);
        String expiredToken = shortLivedJwtService.generateToken(userDetails);

        // Wait to ensure token is expired
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.extractUsername(expiredToken);
        });
    }

    @Test
    @DisplayName("Get expiration time should return correct value")
    void testGetExpirationTime_ReturnsCorrectValue() {
        // Act
        long expirationTime = jwtService.getExpirationTime();

        // Assert
        assertEquals(JWT_EXPIRATION, expirationTime);
    }

    @Test
    @DisplayName("Generated tokens should be different for different users")
    void testGenerateToken_DifferentUsers_DifferentTokens() {
        // Arrange
        UserDetails user1 = User.builder()
                .username("user1@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        UserDetails user2 = User.builder()
                .username("user2@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        // Act
        String token1 = jwtService.generateToken(user1);
        String token2 = jwtService.generateToken(user2);

        // Assert
        assertNotEquals(token1, token2);
        assertEquals("user1@example.com", jwtService.extractUsername(token1));
        assertEquals("user2@example.com", jwtService.extractUsername(token2));
    }

    @Test
    @DisplayName("Token should contain issued at claim")
    void testGenerateToken_ContainsIssuedAt() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        java.util.Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

        // Assert
        assertNotNull(issuedAt);
        assertTrue(issuedAt.getTime() <= System.currentTimeMillis());
    }

    @Test
    @DisplayName("Token should contain expiration claim")
    void testGenerateToken_ContainsExpiration() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        java.util.Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.getTime() > System.currentTimeMillis());
    }
}
