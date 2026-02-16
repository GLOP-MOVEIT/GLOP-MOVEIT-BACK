package com.moveit.user.controller;

import com.moveit.user.dto.Role;
import com.moveit.user.exception.GlobalExceptionHandler;
import com.moveit.user.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    private MockMvc mockMvc;
    private List<Role> testRoles;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testRoles = List.of(
                new Role("SPECTATOR"),
                new Role("VOLUNTEER"),
                new Role("ATHLETE"),
                new Role("ADMIN"),
                new Role("REFEREE")
        );
    }

    @Test
    void getAllRoles_ShouldReturnListOfRoles() throws Exception {
        when(roleService.getAllRole()).thenReturn(testRoles);

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].name").value("SPECTATOR"))
                .andExpect(jsonPath("$[1].name").value("VOLUNTEER"))
                .andExpect(jsonPath("$[2].name").value("ATHLETE"))
                .andExpect(jsonPath("$[3].name").value("ADMIN"))
                .andExpect(jsonPath("$[4].name").value("REFEREE"));

        verify(roleService).getAllRole();
    }

    @Test
    void getAllRoles_ShouldReturnEmptyList_WhenNoRoles() throws Exception {
        when(roleService.getAllRole()).thenReturn(List.of());

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(roleService).getAllRole();
    }
}

