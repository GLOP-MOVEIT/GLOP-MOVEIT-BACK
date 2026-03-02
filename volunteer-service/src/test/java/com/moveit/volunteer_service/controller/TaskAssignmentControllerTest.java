package com.moveit.volunteer_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.volunteer_service.config.TestJacksonConfig;
import com.moveit.volunteer_service.dto.CreateTaskAssignmentRequest;
import com.moveit.volunteer_service.dto.UpdateTaskAssignmentStatusRequest;
import com.moveit.volunteer_service.entity.TaskAssignment;
import com.moveit.volunteer_service.enums.AssignmentStatus;
import com.moveit.volunteer_service.exception.TaskAssignmentNotFoundException;
import com.moveit.volunteer_service.mother.TaskAssignmentMother;
import com.moveit.volunteer_service.service.TaskAssignmentService;
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

@WebMvcTest(controllers = TaskAssignmentController.class)
@Import(TestJacksonConfig.class)
class TaskAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskAssignmentService taskAssignmentService;

    @Test
    @DisplayName("Should return assignments by volunteer id")
    void shouldGetAssignmentsByVolunteerId() throws Exception {
        var assignment = TaskAssignmentMother.defaultAssignment();
        when(taskAssignmentService.getAssignmentsByVolunteerId(10L)).thenReturn(List.of(assignment));

        mockMvc.perform(get("/volunteer/assignments/volunteer/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].volunteerId", equalTo(10)));
    }

    @Test
    @DisplayName("Should return assignments by task id")
    void shouldGetAssignmentsByTaskId() throws Exception {
        var assignment = TaskAssignmentMother.defaultAssignment();
        when(taskAssignmentService.getAssignmentsByTaskId(1L)).thenReturn(List.of(assignment));

        mockMvc.perform(get("/volunteer/assignments/task/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Should return assignments by status")
    void shouldGetAssignmentsByStatus() throws Exception {
        var assignment = TaskAssignmentMother.defaultAssignment();
        when(taskAssignmentService.getAssignmentsByStatus(AssignmentStatus.PENDING))
                .thenReturn(List.of(assignment));

        mockMvc.perform(get("/volunteer/assignments/status/PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Should return assignment by id")
    void shouldGetAssignmentById() throws Exception {
        var assignment = TaskAssignmentMother.defaultAssignment();
        when(taskAssignmentService.getAssignmentById(1L)).thenReturn(assignment);

        mockMvc.perform(get("/volunteer/assignments/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.volunteerId", equalTo(10)));
    }

    @Test
    @DisplayName("Should return 404 when assignment not found")
    void shouldReturn404WhenAssignmentNotFound() throws Exception {
        when(taskAssignmentService.getAssignmentById(99L))
                .thenThrow(new TaskAssignmentNotFoundException(99L));

        mockMvc.perform(get("/volunteer/assignments/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should create an assignment and return 201")
    void shouldCreateAssignment() throws Exception {
        var request = new CreateTaskAssignmentRequest(10L, 1L, "Disponible");
        var saved = TaskAssignmentMother.defaultAssignment();
        when(taskAssignmentService.createAssignment(any(CreateTaskAssignmentRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/volunteer/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.volunteerId", equalTo(10)));
    }

    @Test
    @DisplayName("Should update assignment status")
    void shouldUpdateAssignmentStatus() throws Exception {
        var request = new UpdateTaskAssignmentStatusRequest(AssignmentStatus.ACCEPTED, "Validé");
        var updated = TaskAssignmentMother.defaultAssignment();
        updated.setStatus(AssignmentStatus.ACCEPTED);
        when(taskAssignmentService.updateAssignmentStatus(eq(1L), any(UpdateTaskAssignmentStatusRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/volunteer/assignments/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("ACCEPTED")));
    }

    @Test
    @DisplayName("Should delete an assignment and return 204")
    void shouldDeleteAssignment() throws Exception {
        doNothing().when(taskAssignmentService).deleteAssignment(1L);

        mockMvc.perform(delete("/volunteer/assignments/1"))
                .andExpect(status().isNoContent());
    }
}
