package com.moveit.auth.service;

import com.moveit.auth.dto.RegisterUserDto;
import com.moveit.auth.entity.UserAuth;
import com.moveit.auth.repository.UserAuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

    @Mock
    private UserAuthRepository userAuthRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private UserAuth adminUserAuth;

    @BeforeEach
    void setUp() {
        userService = new UserService(userAuthRepository, passwordEncoder);

        adminUserAuth = new UserAuth()
                .setId(1)
                .setNickname("admin")
                .setPassword("encodedPassword");
    }

    @Test
    void allUsers_ShouldReturnAllUsers() {
        UserAuth userAuth1 = new UserAuth().setId(1).setNickname("user1");
        UserAuth userAuth2 = new UserAuth().setId(2).setNickname("user2");
        List<UserAuth> userAuths = Arrays.asList(userAuth1, userAuth2);

        when(userAuthRepository.findAll()).thenReturn(userAuths);

        List<UserAuth> result = userService.allUsers();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(userAuth1, userAuth2);
    }

    @Test
    void createUser_ShouldCreateUser() {
        RegisterUserDto registerDto = new RegisterUserDto("newuser", "password123");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userAuthRepository.save(any(UserAuth.class))).thenReturn(adminUserAuth);

        UserAuth result = userService.createUser(registerDto);

        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("admin");
        verify(userAuthRepository).save(any(UserAuth.class));
    }

    @Test
    void init_ShouldCreateAdmin_WhenAdminNotExists() {
        when(userAuthRepository.findByNickname("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");

        userService.init();

        verify(userAuthRepository).save(any(UserAuth.class));
    }

    @Test
    void init_ShouldNotCreateAdmin_WhenAdminExists() {
        when(userAuthRepository.findByNickname("admin")).thenReturn(Optional.of(adminUserAuth));


        userService.init();

        verify(userAuthRepository, never()).save(any(UserAuth.class));
    }
}
