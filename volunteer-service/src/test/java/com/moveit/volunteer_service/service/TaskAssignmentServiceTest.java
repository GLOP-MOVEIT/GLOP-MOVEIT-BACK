package com.moveit.volunteer_service.service;

import com.moveit.volunteer_service.dto.CreateTaskAssignmentRequest;
import com.moveit.volunteer_service.dto.UpdateTaskAssignmentStatusRequest;
import com.moveit.volunteer_service.entity.TaskAssignment;
import com.moveit.volunteer_service.entity.VolunteerTask;
import com.moveit.volunteer_service.enums.AssignmentStatus;
import com.moveit.volunteer_service.exception.TaskAssignmentNotFoundException;
import com.moveit.volunteer_service.exception.VolunteerTaskNotFoundException;
import com.moveit.volunteer_service.mother.TaskAssignmentMother;
import com.moveit.volunteer_service.mother.VolunteerTaskMother;
import com.moveit.volunteer_service.repository.TaskAssignmentRepository;
import com.moveit.volunteer_service.repository.VolunteerTaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskAssignmentServiceTest {

    @InjectMocks
    private TaskAssignmentService taskAssignmentService;

    @Mock
    private TaskAssignmentRepository taskAssignmentRepository;

    @Mock
    private VolunteerTaskRepository volunteerTaskRepository;

    @Test
    @DisplayName("Should retrieve assignments by volunteer id")
    void shouldGetAssignmentsByVolunteerId() {
        var assignment = TaskAssignmentMother.defaultAssignment();
        when(taskAssignmentRepository.findByVolunteerId(10L)).thenReturn(List.of(assignment));

        var result = taskAssignmentService.getAssignmentsByVolunteerId(10L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should retrieve assignments by task id")
    void shouldGetAssignmentsByTaskId() {
        var assignment = TaskAssignmentMother.defaultAssignment();
        when(taskAssignmentRepository.findByTaskId(1L)).thenReturn(List.of(assignment));

        var result = taskAssignmentService.getAssignmentsByTaskId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should retrieve assignments by status")
    void shouldGetAssignmentsByStatus() {
        var assignment = TaskAssignmentMother.defaultAssignment();
        when(taskAssignmentRepository.findByStatus(AssignmentStatus.PENDING)).thenReturn(List.of(assignment));

        var result = taskAssignmentService.getAssignmentsByStatus(AssignmentStatus.PENDING);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should retrieve assignment by id")
    void shouldGetAssignmentById() {
        var assignment = TaskAssignmentMother.defaultAssignment();
        when(taskAssignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        var result = taskAssignmentService.getAssignmentById(1L);

        assertThat(result.getVolunteerId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should throw exception when assignment not found")
    void shouldThrowExceptionWhenAssignmentNotFound() {
        when(taskAssignmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskAssignmentService.getAssignmentById(99L))
                .isInstanceOf(TaskAssignmentNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should create an assignment")
    void shouldCreateAssignment() {
        var task = VolunteerTaskMother.defaultTask();
        var request = new CreateTaskAssignmentRequest(10L, 1L, "Comment");
        var saved = TaskAssignmentMother.defaultAssignment();

        when(volunteerTaskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findByVolunteerIdAndTaskId(10L, 1L)).thenReturn(Optional.empty());
        when(taskAssignmentRepository.findByTaskId(1L)).thenReturn(List.of());
        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenReturn(saved);

        var result = taskAssignmentService.createAssignment(request);

        assertThat(result).isNotNull();
        verify(taskAssignmentRepository).save(any(TaskAssignment.class));
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate assignment")
    void shouldThrowExceptionWhenCreatingDuplicateAssignment() {
        var task = VolunteerTaskMother.defaultTask();
        var existing = TaskAssignmentMother.defaultAssignment();
        var request = new CreateTaskAssignmentRequest(10L, 1L, "Comment");

        when(volunteerTaskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findByVolunteerIdAndTaskId(10L, 1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> taskAssignmentService.createAssignment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should throw exception when task is full")
    void shouldThrowExceptionWhenTaskIsFull() {
        var task = VolunteerTaskMother.defaultTask();
        task.setMaxVolunteers(1);
        var existingAssignment = TaskAssignmentMother.defaultAssignment();
        existingAssignment.setStatus(AssignmentStatus.ACCEPTED);
        var request = new CreateTaskAssignmentRequest(20L, 1L, "Comment");

        when(volunteerTaskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findByVolunteerIdAndTaskId(20L, 1L)).thenReturn(Optional.empty());
        when(taskAssignmentRepository.findByTaskId(1L)).thenReturn(List.of(existingAssignment));

        assertThatThrownBy(() -> taskAssignmentService.createAssignment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximum");
    }

    @Test
    @DisplayName("Should throw exception when creating assignment with non-existing task")
    void shouldThrowExceptionWhenCreatingAssignmentWithNonExistingTask() {
        var request = new CreateTaskAssignmentRequest(10L, 99L, "Comment");
        when(volunteerTaskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskAssignmentService.createAssignment(request))
                .isInstanceOf(VolunteerTaskNotFoundException.class);
    }

    @Test
    @DisplayName("Should update assignment status")
    void shouldUpdateAssignmentStatus() {
        var existing = TaskAssignmentMother.defaultAssignment();
        var request = new UpdateTaskAssignmentStatusRequest(AssignmentStatus.ACCEPTED, "Accepted");

        when(taskAssignmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenReturn(existing);

        var result = taskAssignmentService.updateAssignmentStatus(1L, request);

        assertThat(result.getStatus()).isEqualTo(AssignmentStatus.ACCEPTED);
    }

    @Test
    @DisplayName("Should delete an assignment")
    void shouldDeleteAssignment() {
        when(taskAssignmentRepository.existsById(1L)).thenReturn(true);

        taskAssignmentService.deleteAssignment(1L);

        verify(taskAssignmentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existing assignment")
    void shouldThrowExceptionWhenDeletingNonExistingAssignment() {
        when(taskAssignmentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> taskAssignmentService.deleteAssignment(99L))
                .isInstanceOf(TaskAssignmentNotFoundException.class);
    }
}
