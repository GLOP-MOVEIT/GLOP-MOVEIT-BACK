package com.moveit.auth.service;

import com.moveit.auth.entity.Role;
import com.moveit.auth.entity.RoleEnum;
import com.moveit.auth.entity.User;
import com.moveit.auth.model.RegisterUserDto;
import com.moveit.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterUserDto adminDto;
    private User adminUser;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        adminRole = new Role();
        adminRole.setId(1);
        adminRole.setName(RoleEnum.ADMIN);
        adminRole.setDescription("Admin role");

        adminDto = new RegisterUserDto(
                "admin@example.com",
                "password123",
                "Admin",
                "User",
                "+1234567890",
                true,
                true
        );

        adminUser = new User();
        adminUser.setId(1);
        adminUser.setFirstName("Admin");
        adminUser.setSurname("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("encodedPassword");
        adminUser.setPhoneNumber("+1234567890");
        adminUser.setRole(adminRole);
        adminUser.setAcceptsNotifications(true);
        adminUser.setAcceptsLocation(true);
    }

    @Test
    @DisplayName("AllUsers should return list of all users")
    void testAllUsers_Success() {
        // Arrange
        User user1 = new User();
        user1.setId(1);
        user1.setEmail("user1@example.com");
        User user2 = new User();
        user2.setId(2);
        user2.setEmail("user2@example.com");
        List<User> expectedUsers = Arrays.asList(user1, user2);
        
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> result = userService.allUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1@example.com", result.get(0).getEmail());
        assertEquals("user2@example.com", result.get(1).getEmail());
        
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("AllUsers should return empty list when no users exist")
    void testAllUsers_EmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<User> result = userService.allUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("CreateAdministrator should create new admin successfully")
    void testCreateAdministrator_Success() {
        // Arrange
        when(roleService.findByName(RoleEnum.ADMIN)).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        // Act
        User result = userService.createAdministrator(adminDto);

        // Assert
        assertNotNull(result);
        assertEquals("admin@example.com", result.getEmail());
        assertEquals("Admin", result.getFirstName());
        assertEquals("User", result.getSurname());
        
        verify(roleService, times(1)).findByName(RoleEnum.ADMIN);
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("CreateAdministrator should return null when admin role not found")
    void testCreateAdministrator_RoleNotFound_ReturnsNull() {
        // Arrange
        when(roleService.findByName(RoleEnum.ADMIN)).thenReturn(Optional.empty());

        // Act
        User result = userService.createAdministrator(adminDto);

        // Assert
        assertNull(result);
        
        verify(roleService, times(1)).findByName(RoleEnum.ADMIN);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("CreateAdministrator should handle null phone number")
    void testCreateAdministrator_NullPhoneNumber() {
        // Arrange
        RegisterUserDto dtoWithoutPhone = new RegisterUserDto(
                "john@example.com",
                "password123",
                "John",
                "Doe",
                null,
                true,
                true
        );

        when(roleService.findByName(RoleEnum.ADMIN)).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.createAdministrator(dtoWithoutPhone);

        // Assert
        assertNotNull(result);
        assertNull(result.getPhoneNumber());
    }
}
