package com.moveit.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.auth.dto.LoginUserDto;
import com.moveit.auth.dto.RegisterUserDto;
import com.moveit.auth.dto.UserDto;
import com.moveit.auth.entity.Role;
import com.moveit.auth.entity.RoleEnum;
import com.moveit.auth.entity.User;
import com.moveit.auth.mapper.UserMapper;
import com.moveit.auth.service.AuthenticationService;
import com.moveit.auth.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private User testUser;
    private UserDto testUserDto;
    private Role testRole;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        testRole = new Role()
                .setId(1)
                .setName(RoleEnum.SPECTATOR)
                .setDescription("Default user role");

        testUser = new User()
                .setId(1)
                .setNickname("testuser")
                .setPassword("encodedPassword")
                .setRole(testRole)
                .setLastConnectionDate(new Date());

        testUserDto = new UserDto();
        testUserDto.setId(1);
        testUserDto.setNickname("testuser");
        testUserDto.setRole(RoleEnum.SPECTATOR);
        testUserDto.setLastConnectionDate(new Date());
    }

    @Test
    void register_ShouldReturnUserDto_WhenValidInput() throws Exception {
        RegisterUserDto registerDto = new RegisterUserDto("testuser", "password123");

        when(authenticationService.signup(any(RegisterUserDto.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nickname").value("testuser"))
                .andExpect(jsonPath("$.role").value("SPECTATOR"));
    }

    @Test
    void authenticate_ShouldReturnLoginResponse_WhenValidCredentials() throws Exception {
        LoginUserDto loginDto = new LoginUserDto("testuser", "password123");
        String token = "jwt.token.here";
        long expirationTime = 3600000L;

        when(authenticationService.authenticate(any(LoginUserDto.class))).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn(token);
        when(jwtService.getExpirationTime()).thenReturn(expirationTime);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.expiresIn").value(expirationTime))
                .andExpect(jsonPath("$.user.nickname").value("testuser"));
    }

    @Test
    void authenticatedUser_ShouldReturnUserDto_WhenAuthenticated() throws Exception {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);

        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nickname").value("testuser"));

        SecurityContextHolder.clearContext();
    }
}
