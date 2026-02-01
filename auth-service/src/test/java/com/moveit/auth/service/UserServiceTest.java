package com.moveit.auth.service;

import com.moveit.auth.dto.RegisterUserDto;
import com.moveit.auth.entity.User;
import com.moveit.auth.repository.UserRepository;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private User adminUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);

        adminUser = new User()
                .setId(1)
                .setNickname("admin")
                .setPassword("encodedPassword");
    }

    @Test
    void allUsers_ShouldReturnAllUsers() {
        User user1 = new User().setId(1).setNickname("user1");
        User user2 = new User().setId(2).setNickname("user2");
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.allUsers();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(user1, user2);
    }

    @Test
    void createUser_ShouldCreateUser() {
        RegisterUserDto registerDto = new RegisterUserDto("newuser", "password123");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        User result = userService.createUser(registerDto);

        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("admin");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void init_ShouldCreateAdmin_WhenAdminNotExists() {
        when(userRepository.findByNickname("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");

        userService.init();

        verify(userRepository).save(any(User.class));
    }

    @Test
    void init_ShouldNotCreateAdmin_WhenAdminExists() {
        when(userRepository.findByNickname("admin")).thenReturn(Optional.of(adminUser));


        userService.init();

        verify(userRepository, never()).save(any(User.class));
    }
}
