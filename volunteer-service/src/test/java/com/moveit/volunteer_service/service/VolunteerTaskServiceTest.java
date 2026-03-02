package com.moveit.volunteer_service.service;

import com.moveit.volunteer_service.dto.CreateVolunteerTaskRequest;
import com.moveit.volunteer_service.entity.VolunteerTask;
import com.moveit.volunteer_service.enums.TaskStatus;
import com.moveit.volunteer_service.exception.VolunteerTaskNotFoundException;
import com.moveit.volunteer_service.exception.VolunteerTaskTypeNotFoundException;
import com.moveit.volunteer_service.mother.VolunteerTaskMother;
import com.moveit.volunteer_service.mother.VolunteerTaskTypeMother;
import com.moveit.volunteer_service.repository.VolunteerTaskRepository;
import com.moveit.volunteer_service.repository.VolunteerTaskTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class VolunteerTaskServiceTest {

    @InjectMocks
    private VolunteerTaskService volunteerTaskService;

    @Mock
    private VolunteerTaskRepository volunteerTaskRepository;

    @Mock
    private VolunteerTaskTypeRepository volunteerTaskTypeRepository;

    @Test
    @DisplayName("Should retrieve all tasks")
    void shouldGetAllTasks() {
        var task = VolunteerTaskMother.defaultTask();
        when(volunteerTaskRepository.findAll()).thenReturn(List.of(task));

        var result = volunteerTaskService.getAllTasks();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Bénévolat accueil");
    }

    @Test
    @DisplayName("Should retrieve task by id")
    void shouldGetTaskById() {
        var task = VolunteerTaskMother.defaultTask();
        when(volunteerTaskRepository.findById(1L)).thenReturn(Optional.of(task));

        var result = volunteerTaskService.getTaskById(1L);

        assertThat(result.getTitle()).isEqualTo("Bénévolat accueil");
    }

    @Test
    @DisplayName("Should throw exception when task not found")
    void shouldThrowExceptionWhenTaskNotFound() {
        when(volunteerTaskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerTaskService.getTaskById(99L))
                .isInstanceOf(VolunteerTaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should retrieve tasks by championship id")
    void shouldGetTasksByChampionshipId() {
        var task = VolunteerTaskMother.defaultTask();
        when(volunteerTaskRepository.findByChampionshipId(1L)).thenReturn(List.of(task));

        var result = volunteerTaskService.getTasksByChampionshipId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should retrieve tasks by task type id")
    void shouldGetTasksByTaskTypeId() {
        var task = VolunteerTaskMother.defaultTask();
        when(volunteerTaskRepository.findByTaskType_Id(1L)).thenReturn(List.of(task));

        var result = volunteerTaskService.getTasksByTaskTypeId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should create a task")
    void shouldCreateTask() {
        var taskType = VolunteerTaskTypeMother.defaultTaskType();
        var request = new CreateVolunteerTaskRequest(
                1L, "Nouvelle tâche", "Description", 1L,
                LocalDateTime.of(2026, 6, 1, 8, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                5, "Stade"
        );
        var saved = VolunteerTaskMother.defaultTask();
        when(volunteerTaskTypeRepository.findById(1L)).thenReturn(Optional.of(taskType));
        when(volunteerTaskRepository.save(any(VolunteerTask.class))).thenReturn(saved);

        var result = volunteerTaskService.createTask(request);

        assertThat(result).isNotNull();
        verify(volunteerTaskRepository).save(any(VolunteerTask.class));
    }

    @Test
    @DisplayName("Should throw exception when creating task with non-existing task type")
    void shouldThrowExceptionWhenCreatingTaskWithNonExistingType() {
        var request = new CreateVolunteerTaskRequest(
                1L, "Tâche", "Desc", 99L, null, null, 5, null
        );
        when(volunteerTaskTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerTaskService.createTask(request))
                .isInstanceOf(VolunteerTaskTypeNotFoundException.class);
    }

    @Test
    @DisplayName("Should update a task")
    void shouldUpdateTask() {
        var existing = VolunteerTaskMother.defaultTask();
        var taskType = VolunteerTaskTypeMother.defaultTaskType();
        var request = new CreateVolunteerTaskRequest(
                1L, "Tâche mise à jour", "Nouvelle description", 1L,
                LocalDateTime.of(2026, 7, 1, 8, 0),
                LocalDateTime.of(2026, 7, 1, 12, 0),
                10, "Gymnase"
        );
        when(volunteerTaskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(volunteerTaskTypeRepository.findById(1L)).thenReturn(Optional.of(taskType));
        when(volunteerTaskRepository.save(any(VolunteerTask.class))).thenReturn(existing);

        var result = volunteerTaskService.updateTask(1L, request);

        assertThat(result.getTitle()).isEqualTo("Tâche mise à jour");
        verify(volunteerTaskRepository).save(existing);
    }

    @Test
    @DisplayName("Should update task status")
    void shouldUpdateTaskStatus() {
        var existing = VolunteerTaskMother.defaultTask();
        when(volunteerTaskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(volunteerTaskRepository.save(any(VolunteerTask.class))).thenReturn(existing);

        var result = volunteerTaskService.updateTaskStatus(1L, TaskStatus.IN_PROGRESS);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should delete a task")
    void shouldDeleteTask() {
        when(volunteerTaskRepository.existsById(1L)).thenReturn(true);

        volunteerTaskService.deleteTask(1L);

        verify(volunteerTaskRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existing task")
    void shouldThrowExceptionWhenDeletingNonExistingTask() {
        when(volunteerTaskRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> volunteerTaskService.deleteTask(99L))
                .isInstanceOf(VolunteerTaskNotFoundException.class);
    }
}
