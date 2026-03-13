package com.moveit.volunteer_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.volunteer_service.config.TestJacksonConfig;
import com.moveit.volunteer_service.dto.CreateVolunteerTaskRequest;
import com.moveit.volunteer_service.enums.TaskStatus;
import com.moveit.volunteer_service.enums.TaskTargetType;
import com.moveit.volunteer_service.exception.VolunteerTaskNotFoundException;
import com.moveit.volunteer_service.mother.VolunteerTaskMother;
import com.moveit.volunteer_service.service.VolunteerTaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = VolunteerTaskController.class)
@Import(TestJacksonConfig.class)
class VolunteerTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VolunteerTaskService volunteerTaskService;

    @Test
    @DisplayName("Should return all tasks")
    void shouldGetAllTasks() throws Exception {
        var task = VolunteerTaskMother.defaultTask();
        when(volunteerTaskService.getAllTasks()).thenReturn(List.of(task));

        mockMvc.perform(get("/volunteer/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", equalTo("Bénévolat accueil")));
    }

    @Test
    @DisplayName("Should return task by id")
    void shouldGetTaskById() throws Exception {
        var task = VolunteerTaskMother.defaultTask();
        when(volunteerTaskService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/volunteer/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", equalTo("Bénévolat accueil")));
    }

    @Test
    @DisplayName("Should return 404 when task not found")
    void shouldReturn404WhenTaskNotFound() throws Exception {
        when(volunteerTaskService.getTaskById(99L))
                .thenThrow(new VolunteerTaskNotFoundException(99L));

        mockMvc.perform(get("/volunteer/tasks/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
        @DisplayName("Should return tasks by target")
        void shouldGetTasksByTarget() throws Exception {
        var task = VolunteerTaskMother.defaultTask();
                when(volunteerTaskService.getTasksByTarget(TaskTargetType.CHAMPIONSHIP, 1L)).thenReturn(List.of(task));

                mockMvc.perform(get("/volunteer/tasks/target/CHAMPIONSHIP/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Should return tasks by task type id")
    void shouldGetTasksByTaskTypeId() throws Exception {
        var task = VolunteerTaskMother.defaultTask();
        when(volunteerTaskService.getTasksByTaskTypeId(1L)).thenReturn(List.of(task));

        mockMvc.perform(get("/volunteer/tasks/type/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Should create a task and return 201")
    void shouldCreateTask() throws Exception {
        var request = new CreateVolunteerTaskRequest(
                                TaskTargetType.CHAMPIONSHIP, 1L, "Nouvelle tâche", "Description", 1L,
                LocalDateTime.of(2026, 6, 1, 8, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                5, 1L, "Stade"
        );
        var saved = VolunteerTaskMother.defaultTask();
        when(volunteerTaskService.createTask(any(CreateVolunteerTaskRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/volunteer/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", equalTo("Bénévolat accueil")));
    }

    @Test
    @DisplayName("Should update a task")
    void shouldUpdateTask() throws Exception {
        var request = new CreateVolunteerTaskRequest(
                                TaskTargetType.CHAMPIONSHIP, 1L, "Tâche mise à jour", "Desc", 1L,
                LocalDateTime.of(2026, 7, 1, 8, 0),
                LocalDateTime.of(2026, 7, 1, 12, 0),
                10, 1L, "Gymnase"
        );
        var updated = VolunteerTaskMother.defaultTask();
        updated.setTitle("Tâche mise à jour");
        when(volunteerTaskService.updateTask(eq(1L), any(CreateVolunteerTaskRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/volunteer/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", equalTo("Tâche mise à jour")));
    }

    @Test
    @DisplayName("Should update task status")
    void shouldUpdateTaskStatus() throws Exception {
        var updated = VolunteerTaskMother.defaultTask();
        updated.setStatus(TaskStatus.IN_PROGRESS);
        when(volunteerTaskService.updateTaskStatus(1L, TaskStatus.IN_PROGRESS)).thenReturn(updated);

        mockMvc.perform(patch("/volunteer/tasks/1/status")
                        .param("status", "IN_PROGRESS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("IN_PROGRESS")));
    }

    @Test
    @DisplayName("Should delete a task and return 204")
    void shouldDeleteTask() throws Exception {
        doNothing().when(volunteerTaskService).deleteTask(1L);

        mockMvc.perform(delete("/volunteer/tasks/1"))
                .andExpect(status().isNoContent());
    }
}
