package com.moveit.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private String secretKey;
    private String validToken;
    private String expiredToken;

    @BeforeEach
    void setUp() {
        secretKey = "bW92ZWl0U2VjcmV0S2V5Rm9ySlNPTldlYlRva2VuU2lnbmluZ0FuZEF1dGhlbnRpY2F0aW9u";
        jwtService = new JwtService(secretKey);
        
        // Créer un token valide
        validToken = createToken("testuser", System.currentTimeMillis() + 3600000); // Expire dans 1h
        
        // Créer un token expiré
        expiredToken = createToken("testuser", System.currentTimeMillis() - 3600000); // Expiré depuis 1h
    }

    private String createToken(String username, long expirationTime) {
        SecretKey key = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secretKey));
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(expirationTime))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("Should extract username from valid token")
    void testExtractUsername() {
        String username = jwtService.extractUsername(validToken);
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should validate a valid token")
    void testIsTokenValid_ValidToken() {
        boolean isValid = jwtService.isTokenValid(validToken);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject an expired token")
    void testIsTokenValid_ExpiredToken() {
        boolean isValid = jwtService.isTokenValid(expiredToken);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject an invalid token")
    void testIsTokenValid_InvalidToken() {
        String invalidToken = "invalid.token.here";
        assertThrows(RuntimeException.class, () -> {
            jwtService.isTokenValid(invalidToken);
        });
    }

    @Test
    @DisplayName("Should extract claim from token")
    void testExtractClaim() {
        String username = jwtService.extractClaim(validToken, Claims::getSubject);
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should throw exception for malformed token")
    void testExtractUsername_MalformedToken() {
        assertThrows(RuntimeException.class, () -> {
            jwtService.extractUsername("malformed.token");
        });
    }

    @Test
    @DisplayName("Should handle null token gracefully")
    void testIsTokenValid_NullToken() {
        assertThrows(Exception.class, () -> {
            jwtService.isTokenValid(null);
        });
    }

    @Test
    @DisplayName("Should handle empty token gracefully")
    void testIsTokenValid_EmptyToken() {
        assertThrows(RuntimeException.class, () -> {
            jwtService.isTokenValid("");
        });
    }
}
