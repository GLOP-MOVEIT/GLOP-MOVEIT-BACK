package com.moveit.auth.service;

import com.moveit.auth.entity.Role;
import com.moveit.auth.entity.RoleEnum;
import com.moveit.auth.entity.User;
import com.moveit.auth.model.LoginUserDto;
import com.moveit.auth.model.RegisterUserDto;
import com.moveit.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterUserDto registerUserDto;
    private LoginUserDto loginUserDto;
    private User user;
    private Role spectatorRole;

    @BeforeEach
    void setUp() {
        spectatorRole = new Role();
        spectatorRole.setId(1);
        spectatorRole.setName(RoleEnum.SPECTATOR);
        spectatorRole.setDescription("Spectator role");

        registerUserDto = new RegisterUserDto(
                "john.doe@example.com",
                "password123",
                "John",
                "Doe",
                "+1234567890",
                true,
                true
        );

        loginUserDto = new LoginUserDto(
                "john.doe@example.com",
                "password123"
        );

        user = new User();
        user.setId(1);
        user.setFirstName("John");
        user.setSurname("Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("encodedPassword");
        user.setPhoneNumber("+1234567890");
        user.setRole(spectatorRole);
        user.setAcceptsNotifications(true);
        user.setAcceptsLocation(true);
    }

    @Test
    @DisplayName("Signup should create new user successfully")
    void testSignup_Success() {
        // Arrange
        when(roleService.findByName(RoleEnum.SPECTATOR)).thenReturn(Optional.of(spectatorRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = authenticationService.signup(registerUserDto);

        // Assert
        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getSurname());
        
        verify(roleService, times(1)).findByName(RoleEnum.SPECTATOR);
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Signup should return null when role not found")
    void testSignup_RoleNotFound_ReturnsNull() {
        // Arrange
        when(roleService.findByName(RoleEnum.SPECTATOR)).thenReturn(Optional.empty());

        // Act
        User result = authenticationService.signup(registerUserDto);

        // Assert
        assertNull(result);
        verify(roleService, times(1)).findByName(RoleEnum.SPECTATOR);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Authenticate should authenticate user successfully")
    void testAuthenticate_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        // Act
        var result = authenticationService.authenticate(loginUserDto);

        // Assert
        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getUsername());
        
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Authenticate should throw exception for invalid credentials")
    void testAuthenticate_InvalidCredentials_ThrowsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(loginUserDto);
        });

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Authenticate should throw exception when user not found")
    void testAuthenticate_UserNotFound_ThrowsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> {
            authenticationService.authenticate(loginUserDto);
        });

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail("john.doe@example.com");
    }
}
