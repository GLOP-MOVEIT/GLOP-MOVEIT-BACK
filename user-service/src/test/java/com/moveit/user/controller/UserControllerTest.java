package com.moveit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.user.dto.Role;
import com.moveit.user.dto.User;
import com.moveit.user.dto.UserRequest;
import com.moveit.user.exception.GlobalExceptionHandler;
import com.moveit.user.exception.UserNotFoundException;
import com.moveit.user.service.UserService;
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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;
    private UserRequest testUserRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        Role testRole = new Role("SPECTATOR");

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
    void getAllUsers_ShouldReturnPageOfUsers() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.content[0].surname").value("Doe"))
                .andExpect(jsonPath("$.content[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(userService).getAllUsers(any(Pageable.class));
    }

    @Test
    void getUserProfile_ShouldReturnUser_WhenUserExists() throws Exception {
        when(userService.getUserById(1)).thenReturn(testUser);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.language").value("fr"))
                .andExpect(jsonPath("$.role.name").value("SPECTATOR"))
                .andExpect(jsonPath("$.acceptsNotifications").value(true))
                .andExpect(jsonPath("$.acceptsLocationSharing").value(false));

        verify(userService).getUserById(1);
    }

    @Test
    void getUserProfile_ShouldReturn404_WhenUserNotFound() throws Exception {
        when(userService.getUserById(999)).thenThrow(new UserNotFoundException(999));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("User not found with id: 999"));

        verify(userService).getUserById(999);
    }

    @Test
    void updateUserProfile_ShouldReturnUpdatedUser_WhenValidInput() throws Exception {
        when(userService.updateUser(any(UserRequest.class))).thenReturn(testUser);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService).updateUser(any(UserRequest.class));
    }

    @Test
    void updateUserProfile_ShouldReturn404_WhenUserNotFound() throws Exception {
        when(userService.updateUser(any(UserRequest.class))).thenThrow(new UserNotFoundException(999));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(userService).updateUser(any(UserRequest.class));
    }

    @Test
    void updateUserProfile_ShouldReturn400_WhenInvalidInput() throws Exception {
        UserRequest invalidRequest = new UserRequest();
        invalidRequest.setUserId(1);
        // Missing required fields

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSpectator_ShouldReturnCreatedUser_WhenValidInput() throws Exception {
        when(userService.createSpectator(any(UserRequest.class))).thenReturn(testUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.role.name").value("SPECTATOR"));

        verify(userService).createSpectator(any(UserRequest.class));
    }

    @Test
    void createSpectator_ShouldReturn400_WhenInvalidInput() throws Exception {
        UserRequest invalidRequest = new UserRequest();
        // Missing required fields

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}





