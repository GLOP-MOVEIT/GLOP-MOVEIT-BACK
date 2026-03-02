package com.moveit.volunteer_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.volunteer_service.config.TestJacksonConfig;
import com.moveit.volunteer_service.dto.CreateVolunteerTaskTypeRequest;
import com.moveit.volunteer_service.entity.VolunteerTaskType;
import com.moveit.volunteer_service.exception.VolunteerTaskTypeNotFoundException;
import com.moveit.volunteer_service.mother.VolunteerTaskTypeMother;
import com.moveit.volunteer_service.service.VolunteerTaskTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = VolunteerTaskTypeController.class)
@Import(TestJacksonConfig.class)
class VolunteerTaskTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VolunteerTaskTypeService volunteerTaskTypeService;

    @Test
    @DisplayName("Should return all task types")
    void shouldGetAllTaskTypes() throws Exception {
        var taskType = VolunteerTaskTypeMother.defaultTaskType();
        when(volunteerTaskTypeService.getAllTaskTypes()).thenReturn(List.of(taskType));

        mockMvc.perform(get("/volunteer/task-types")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", equalTo("Accueil")));
    }

    @Test
    @DisplayName("Should return task type by id")
    void shouldGetTaskTypeById() throws Exception {
        var taskType = VolunteerTaskTypeMother.defaultTaskType();
        when(volunteerTaskTypeService.getTaskTypeById(1L)).thenReturn(taskType);

        mockMvc.perform(get("/volunteer/task-types/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Accueil")));
    }

    @Test
    @DisplayName("Should return 404 when task type not found")
    void shouldReturn404WhenTaskTypeNotFound() throws Exception {
        when(volunteerTaskTypeService.getTaskTypeById(99L))
                .thenThrow(new VolunteerTaskTypeNotFoundException(99L));

        mockMvc.perform(get("/volunteer/task-types/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should create a task type and return 201")
    void shouldCreateTaskType() throws Exception {
        var request = new CreateVolunteerTaskTypeRequest("Sécurité", "Gestion sécurité");
        var saved = VolunteerTaskTypeMother.taskType(2L, "Sécurité", "Gestion sécurité");
        when(volunteerTaskTypeService.createTaskType(any(CreateVolunteerTaskTypeRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/volunteer/task-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", equalTo("Sécurité")));
    }

    @Test
    @DisplayName("Should update a task type")
    void shouldUpdateTaskType() throws Exception {
        var request = new CreateVolunteerTaskTypeRequest("Accueil VIP", "Accueil VIP");
        var updated = VolunteerTaskTypeMother.taskType(1L, "Accueil VIP", "Accueil VIP");
        when(volunteerTaskTypeService.updateTaskType(eq(1L), any(CreateVolunteerTaskTypeRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/volunteer/task-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Accueil VIP")));
    }

    @Test
    @DisplayName("Should delete a task type and return 204")
    void shouldDeleteTaskType() throws Exception {
        doNothing().when(volunteerTaskTypeService).deleteTaskType(1L);

        mockMvc.perform(delete("/volunteer/task-types/1"))
                .andExpect(status().isNoContent());
    }
}
