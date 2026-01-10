package com.moveit.common.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter writer;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private String validToken;
    private String secretKey;

    @BeforeEach
    void setUp() throws IOException {
        SecurityContextHolder.clearContext();
        secretKey = "bW92ZWl0U2VjcmV0S2V5Rm9ySlNPTldlYlRva2VuU2lnbmluZ0FuZEF1dGhlbnRpY2F0aW9u";
        validToken = createToken("testuser", System.currentTimeMillis() + 3600000);
        lenient().when(response.getWriter()).thenReturn(writer);
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
    @DisplayName("Should pass through when no Authorization header is present")
    void testDoFilterInternal_NoAuthHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should pass through when Authorization header does not start with Bearer")
    void testDoFilterInternal_InvalidAuthHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should authenticate with valid JWT token")
    void testDoFilterInternal_ValidToken() throws ServletException, IOException {
        String authHeader = "Bearer " + validToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(validToken)).thenReturn("testuser");
        when(jwtService.isTokenValid(validToken)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("testuser", auth.getPrincipal());
    }

    @Test
    @DisplayName("Should reject invalid JWT token")
    void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
        String authHeader = "Bearer invalidtoken";
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername("invalidtoken")).thenThrow(new RuntimeException("Invalid token"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Token JWT invalide ou expiré");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Should reject expired JWT token")
    void testDoFilterInternal_ExpiredToken() throws ServletException, IOException {
        String expiredToken = createToken("testuser", System.currentTimeMillis() - 3600000);
        String authHeader = "Bearer " + expiredToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(expiredToken)).thenReturn("testuser");
        when(jwtService.isTokenValid(expiredToken)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    @Test
    @DisplayName("Should not authenticate when authentication already exists")
    void testDoFilterInternal_AuthenticationAlreadyExists() throws ServletException, IOException {
        String authHeader = "Bearer " + validToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(validToken)).thenReturn("testuser");
        
        // Simuler une authentification existante
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("existinguser", null);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("existinguser", auth.getPrincipal());
    }

    @Test
    @DisplayName("Should handle null username from token")
    void testDoFilterInternal_NullUsername() throws ServletException, IOException {
        String authHeader = "Bearer " + validToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(validToken)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
