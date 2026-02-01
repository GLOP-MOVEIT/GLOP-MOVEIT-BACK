package com.moveit.auth.service;

import com.moveit.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;
    private static final String SECRET_KEY = "dGhpc2lzYXZlcnlsb25nc2VjcmV0a2V5Zm9ydGVzdGluZ3B1cnBvc2VzYW5kaXRzaG91bGRiZWF0bGVhc3QyNTZiaXRz";
    private static final long EXPIRATION_TIME = 3600000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET_KEY, EXPIRATION_TIME);

        testUser = new User()
                .setId(1)
                .setNickname("testuser")
                .setPassword("encodedPassword");
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtService.generateToken(testUser);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateToken_WithExtraClaims_ShouldReturnValidToken() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "SPECTATOR");

        String token = jwtService.generateToken(extraClaims, testUser);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void generateToken_WithNullExtraClaims_ShouldReturnValidToken() {
        String token = jwtService.generateToken(null, testUser);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtService.generateToken(testUser);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenIsValid() {
        String token = jwtService.generateToken(testUser);

        boolean isValid = jwtService.isTokenValid(token, testUser);

        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenUsernameMismatch() {
        String token = jwtService.generateToken(testUser);

        User differentUser = new User()
                .setId(2)
                .setNickname("differentuser")
                .setPassword("pass");

        boolean isValid = jwtService.isTokenValid(token, differentUser);

        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.here";

        assertThatThrownBy(() -> jwtService.isTokenValid(invalidToken, testUser))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsExpired() {
        JwtService shortExpirationService = new JwtService(SECRET_KEY, 1L);
        String token = shortExpirationService.generateToken(testUser);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean isValid = shortExpirationService.isTokenValid(token, testUser);

        assertThat(isValid).isFalse();
    }

    @Test
    void getExpirationTime_ShouldReturnConfiguredValue() {
        long expirationTime = jwtService.getExpirationTime();

        assertThat(expirationTime).isEqualTo(EXPIRATION_TIME);
    }

    @Test
    void extractUsername_ShouldThrowException_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.here";

        assertThatThrownBy(() -> jwtService.extractUsername(invalidToken))
                .isInstanceOf(RuntimeException.class);
    }
}
