package com.moveit.user.service;

import com.moveit.user.dto.Role;
import com.moveit.user.dto.User;
import com.moveit.user.dto.UserRequest;
import com.moveit.user.entity.RoleEntity;
import com.moveit.user.entity.UserEntity;
import com.moveit.user.exception.UserNotFoundException;
import com.moveit.user.mapper.UserMapper;
import com.moveit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UserEntity testUserEntity;
    private User testUser;
    private UserRequest testUserRequest;
    private RoleEntity testRoleEntity;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRoleEntity = new RoleEntity();
        testRoleEntity.setName("SPECTATOR");

        testRole = new Role("SPECTATOR");

        testUserEntity = new UserEntity();
        testUserEntity.setUserId(1);
        testUserEntity.setFirstName("John");
        testUserEntity.setSurname("Doe");
        testUserEntity.setEmail("john.doe@example.com");
        testUserEntity.setPhoneNumber("123456789");
        testUserEntity.setLanguage("fr");
        testUserEntity.setRole(testRoleEntity);
        testUserEntity.setAcceptsNotifications(true);
        testUserEntity.setAcceptsLocationSharing(false);

        testUser = new User();
        testUser.setUserId(1);
        testUser.setFirstName("John");
        testUser.setSurname("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPhoneNumber("123456789");
        testUser.setLanguage("fr");
        testUser.setRole(testRole);
        testUser.setAcceptsNotifications(true);
        testUser.setAcceptsLocationSharing(false);

        testUserRequest = new UserRequest();
        testUserRequest.setUserId(1);
        testUserRequest.setFirstName("John");
        testUserRequest.setSurname("Doe");
        testUserRequest.setEmail("john.doe@example.com");
        testUserRequest.setPhoneNumber("123456789");
        testUserRequest.setLanguage("fr");
        testUserRequest.setAcceptsNotifications(true);
        testUserRequest.setAcceptsLocationSharing(false);
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> userEntityPage = new PageImpl<>(List.of(testUserEntity), pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(userEntityPage);
        when(userMapper.toDto(testUserEntity)).thenReturn(testUser);

        Page<User> result = userService.getAllUsers(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
        assertThat(result.getContent().get(0).getSurname()).isEqualTo("Doe");
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(userRepository).findAll(pageable);
        verify(userMapper).toDto(testUserEntity);
    }

    @Test
    void getAllUsers_ShouldReturnEmptyPage_WhenNoUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<User> result = userService.getAllUsers(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(userRepository).findAll(pageable);
        verify(userMapper, never()).toDto(any(UserEntity.class));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUserEntity));
        when(userMapper.toDto(testUserEntity)).thenReturn(testUser);

        User result = userService.getUserById(1);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getSurname()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");

        verify(userRepository).findById(1);
        verify(userMapper).toDto(testUserEntity);
    }

    @Test
    void getUserById_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(999);
        verify(userMapper, never()).toDto(any(UserEntity.class));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenUserExists() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUserEntity));
        when(userMapper.toEntity(testUserRequest)).thenReturn(testUserEntity);
        when(userRepository.save(testUserEntity)).thenReturn(testUserEntity);
        when(userMapper.toDto(testUserEntity)).thenReturn(testUser);

        User result = userService.updateUser(testUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getSurname()).isEqualTo("Doe");

        verify(userRepository).findById(1);
        verify(userMapper).toEntity(testUserRequest);
        verify(userRepository).save(testUserEntity);
        verify(userMapper).toDto(testUserEntity);
    }

    @Test
    void updateUser_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        testUserRequest.setUserId(999);
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(testUserRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(999);
        verify(userMapper, never()).toEntity(any(UserRequest.class));
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(userMapper, never()).toDto(any(UserEntity.class));
    }

    @Test
    void createSpectator_ShouldCreateAndReturnUser() {
        when(userMapper.toEntity(testUserRequest)).thenReturn(testUserEntity);
        when(userRepository.save(testUserEntity)).thenReturn(testUserEntity);
        when(userMapper.toDto(testUserEntity)).thenReturn(testUser);

        User result = userService.createSpectator(testUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getSurname()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getRole().getName()).isEqualTo("SPECTATOR");

        verify(userMapper).toEntity(testUserRequest);
        verify(userRepository).save(testUserEntity);
        verify(userMapper).toDto(testUserEntity);
    }

    @Test
    void createSpectator_ShouldMapAllFieldsCorrectly() {
        UserRequest newUserRequest = new UserRequest();
        newUserRequest.setFirstName("Jane");
        newUserRequest.setSurname("Smith");
        newUserRequest.setEmail("jane.smith@example.com");
        newUserRequest.setPhoneNumber("987654321");
        newUserRequest.setLanguage("en");
        newUserRequest.setAcceptsNotifications(false);
        newUserRequest.setAcceptsLocationSharing(true);

        UserEntity newUserEntity = new UserEntity();
        newUserEntity.setUserId(2);
        newUserEntity.setFirstName("Jane");
        newUserEntity.setSurname("Smith");
        newUserEntity.setEmail("jane.smith@example.com");
        newUserEntity.setPhoneNumber("987654321");
        newUserEntity.setLanguage("en");
        newUserEntity.setRole(testRoleEntity);
        newUserEntity.setAcceptsNotifications(false);
        newUserEntity.setAcceptsLocationSharing(true);

        User newUser = new User();
        newUser.setUserId(2);
        newUser.setFirstName("Jane");
        newUser.setSurname("Smith");
        newUser.setEmail("jane.smith@example.com");
        newUser.setPhoneNumber("987654321");
        newUser.setLanguage("en");
        newUser.setRole(testRole);
        newUser.setAcceptsNotifications(false);
        newUser.setAcceptsLocationSharing(true);

        when(userMapper.toEntity(newUserRequest)).thenReturn(newUserEntity);
        when(userRepository.save(newUserEntity)).thenReturn(newUserEntity);
        when(userMapper.toDto(newUserEntity)).thenReturn(newUser);

        User result = userService.createSpectator(newUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getSurname()).isEqualTo("Smith");
        assertThat(result.getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(result.getPhoneNumber()).isEqualTo("987654321");
        assertThat(result.getLanguage()).isEqualTo("en");
        assertThat(result.isAcceptsNotifications()).isFalse();
        assertThat(result.isAcceptsLocationSharing()).isTrue();

        verify(userMapper).toEntity(newUserRequest);
        verify(userRepository).save(newUserEntity);
        verify(userMapper).toDto(newUserEntity);
    }
}

