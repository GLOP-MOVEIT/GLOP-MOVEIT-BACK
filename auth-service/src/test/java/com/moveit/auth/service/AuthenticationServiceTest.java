package com.moveit.auth.service;

import com.moveit.auth.dto.LoginUserDto;
import com.moveit.auth.dto.RegisterUserDto;
import com.moveit.auth.entity.Role;
import com.moveit.auth.entity.RoleEnum;
import com.moveit.auth.entity.User;
import com.moveit.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

    private Role spectatorRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        spectatorRole = new Role()
                .setId(1)
                .setName(RoleEnum.SPECTATOR)
                .setDescription("Default user role");

        testUser = new User()
                .setId(1)
                .setNickname("testuser")
                .setPassword("encodedPassword")
                .setRole(spectatorRole);
    }

    @Test
    void signup_ShouldCreateUser_WhenRoleExists() {
        RegisterUserDto registerDto = new RegisterUserDto("testuser", "password123");

        when(roleService.findByName(RoleEnum.SPECTATOR)).thenReturn(Optional.of(spectatorRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authenticationService.signup(registerDto);

        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("testuser");
        assertThat(result.getRole()).isEqualTo(spectatorRole);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_ShouldThrowException_WhenRoleNotFound() {
        RegisterUserDto registerDto = new RegisterUserDto("testuser", "password123");

        when(roleService.findByName(RoleEnum.SPECTATOR)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.signup(registerDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Default role not found");
    }

    @Test
    void authenticate_ShouldReturnUser_WhenCredentialsValid() {
        LoginUserDto loginDto = new LoginUserDto("testuser", "password123");

        when(userRepository.findByNickname("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authenticationService.authenticate(loginDto);

        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("testuser");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void authenticate_ShouldThrowException_WhenUserNotFound() {
        LoginUserDto loginDto = new LoginUserDto("unknownuser", "password123");

        when(userRepository.findByNickname("unknownuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.authenticate(loginDto))
                .isInstanceOf(NoSuchElementException.class);
    }
}
