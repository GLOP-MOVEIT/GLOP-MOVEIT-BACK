package com.moveit.auth.service;

import com.moveit.auth.dto.LoginUserDto;
import com.moveit.auth.dto.RegisterUserDto;
import com.moveit.auth.entity.UserAuth;
import com.moveit.auth.repository.UserAuthRepository;
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
    private UserAuthRepository userAuthRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UserAuth testUserAuth;

    @BeforeEach
    void setUp() {
        testUserAuth = new UserAuth()
                .setId(1)
                .setNickname("testuser")
                .setPassword("encodedPassword");
    }

    @Test
    void signup_ShouldCreateUser() {
        RegisterUserDto registerDto = new RegisterUserDto("testuser", "password123");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userAuthRepository.save(any(UserAuth.class))).thenReturn(testUserAuth);

        UserAuth result = authenticationService.signup(registerDto);

        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("testuser");
        verify(userAuthRepository).save(any(UserAuth.class));
    }


    @Test
    void authenticate_ShouldReturnUser_WhenCredentialsValid() {
        LoginUserDto loginDto = new LoginUserDto("testuser", "password123");

        when(userAuthRepository.findByNickname("testuser")).thenReturn(Optional.of(testUserAuth));
        when(userAuthRepository.save(any(UserAuth.class))).thenReturn(testUserAuth);

        UserAuth result = authenticationService.authenticate(loginDto);

        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("testuser");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userAuthRepository).save(any(UserAuth.class));
    }

    @Test
    void authenticate_ShouldThrowException_WhenUserNotFound() {
        LoginUserDto loginDto = new LoginUserDto("unknownuser", "password123");

        when(userAuthRepository.findByNickname("unknownuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.authenticate(loginDto))
                .isInstanceOf(NoSuchElementException.class);
    }
}
