package com.moveit.volunteer_service.service;

import com.moveit.volunteer_service.dto.CreateTaskAssignmentRequest;
import com.moveit.volunteer_service.dto.VolunteerAssignmentResponseRequest;
import com.moveit.volunteer_service.entity.VolunteerPreference;
import com.moveit.volunteer_service.entity.TaskAssignment;
import com.moveit.volunteer_service.enums.AssignmentStatus;
import com.moveit.volunteer_service.exception.TaskAssignmentNotFoundException;
import com.moveit.volunteer_service.exception.VolunteerTaskNotFoundException;
import com.moveit.volunteer_service.mother.TaskAssignmentMother;
import com.moveit.volunteer_service.mother.VolunteerTaskMother;
import com.moveit.volunteer_service.repository.TaskAssignmentRepository;
import com.moveit.volunteer_service.repository.VolunteerPreferenceRepository;
import com.moveit.volunteer_service.repository.VolunteerTaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskAssignmentServiceTest {

    @InjectMocks
    private TaskAssignmentService taskAssignmentService;

    @Mock
    private TaskAssignmentRepository taskAssignmentRepository;

    @Mock
    private VolunteerTaskRepository volunteerTaskRepository;

    @Mock
    private VolunteerPreferenceRepository volunteerPreferenceRepository;

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
    @DisplayName("Should return only available volunteers for task")
    void shouldReturnOnlyAvailableVolunteersForTask() {
        var task = VolunteerTaskMother.defaultTask();
        task.setId(1L);
        task.setStartDate(LocalDateTime.of(2026, 6, 1, 8, 0));
        task.setEndDate(LocalDateTime.of(2026, 6, 1, 12, 0));

        var conflictingTask = VolunteerTaskMother.defaultTask();
        conflictingTask.setId(2L);
        conflictingTask.setStartDate(LocalDateTime.of(2026, 6, 1, 9, 0));
        conflictingTask.setEndDate(LocalDateTime.of(2026, 6, 1, 11, 0));

        var busyAssignment = TaskAssignmentMother.defaultAssignment();
        busyAssignment.setVolunteerId(20L);
        busyAssignment.setTask(conflictingTask);
        busyAssignment.setStatus(AssignmentStatus.ACCEPTED);

        when(volunteerTaskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findByVolunteerIdIn(List.of(10L, 20L, 30L)))
                .thenReturn(List.of(busyAssignment));

        var result = taskAssignmentService.getAvailableVolunteersForTask(1L, List.of(10L, 20L, 30L));

        assertThat(result).containsExactly(10L, 30L);
    }

    @Test
    @DisplayName("Should return available volunteers with preference first")
    void shouldReturnAvailableVolunteersWithPreferenceForTask() {
        var task = VolunteerTaskMother.defaultTask();
        task.setId(1L);
        task.setStartDate(LocalDateTime.of(2026, 6, 1, 8, 0));
        task.setEndDate(LocalDateTime.of(2026, 6, 1, 12, 0));
        task.getTaskType().setId(5L);

        var conflictingTask = VolunteerTaskMother.defaultTask();
        conflictingTask.setId(2L);
        conflictingTask.setStartDate(LocalDateTime.of(2026, 6, 1, 9, 0));
        conflictingTask.setEndDate(LocalDateTime.of(2026, 6, 1, 11, 0));

        var busyAssignment = TaskAssignmentMother.defaultAssignment();
        busyAssignment.setVolunteerId(20L);
        busyAssignment.setTask(conflictingTask);
        busyAssignment.setStatus(AssignmentStatus.ACCEPTED);

        VolunteerPreference preferred = new VolunteerPreference();
        preferred.setUserId(10L);
        preferred.setTaskType(task.getTaskType());

        when(volunteerTaskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findByVolunteerIdIn(List.of(10L, 20L, 30L)))
                .thenReturn(List.of(busyAssignment));
        when(volunteerPreferenceRepository.findByTaskType_IdAndUserIdIn(5L, List.of(10L, 30L)))
                .thenReturn(List.of(preferred));

        var result = taskAssignmentService.getAvailableVolunteersWithPreferenceForTask(1L, List.of(10L, 20L, 30L));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getVolunteerId()).isEqualTo(10L);
        assertThat(result.get(0).getHasPreference()).isTrue();
        assertThat(result.get(1).getVolunteerId()).isEqualTo(30L);
        assertThat(result.get(1).getHasPreference()).isFalse();
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
        var request = new CreateTaskAssignmentRequest(10L, 1L);
        var saved = TaskAssignmentMother.defaultAssignment();

        when(volunteerTaskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findByVolunteerIdAndTaskId(10L, 1L)).thenReturn(Optional.empty());
        when(taskAssignmentRepository.findByVolunteerId(10L)).thenReturn(List.of());
        when(taskAssignmentRepository.findByTaskId(1L)).thenReturn(List.of());
        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenReturn(saved);

        var result = taskAssignmentService.createAssignment(request);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate assignment")
    void shouldThrowExceptionWhenCreatingDuplicateAssignment() {
        var task = VolunteerTaskMother.defaultTask();
        var existing = TaskAssignmentMother.defaultAssignment();
        var request = new CreateTaskAssignmentRequest(10L, 1L);

        when(volunteerTaskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findByVolunteerIdAndTaskId(10L, 1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> taskAssignmentService.createAssignment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should throw exception when volunteer has overlapping assignment")
    void shouldThrowExceptionWhenVolunteerHasOverlappingAssignment() {
        var task = VolunteerTaskMother.defaultTask();
        task.setId(1L);
        task.setStartDate(LocalDateTime.of(2026, 6, 1, 8, 0));
        task.setEndDate(LocalDateTime.of(2026, 6, 1, 12, 0));

        var conflictingTask = VolunteerTaskMother.defaultTask();
        conflictingTask.setId(2L);
        conflictingTask.setStartDate(LocalDateTime.of(2026, 6, 1, 11, 0));
        conflictingTask.setEndDate(LocalDateTime.of(2026, 6, 1, 14, 0));

        var existingAssignment = TaskAssignmentMother.defaultAssignment();
        existingAssignment.setVolunteerId(20L);
        existingAssignment.setTask(conflictingTask);
        existingAssignment.setStatus(AssignmentStatus.ACCEPTED);

        var request = new CreateTaskAssignmentRequest(20L, 1L);

        when(volunteerTaskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findByVolunteerIdAndTaskId(20L, 1L)).thenReturn(Optional.empty());
        when(taskAssignmentRepository.findByVolunteerId(20L)).thenReturn(List.of(existingAssignment));

        assertThatThrownBy(() -> taskAssignmentService.createAssignment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("time slot");
    }

    @Test
    @DisplayName("Should throw exception when task is full")
    void shouldThrowExceptionWhenTaskIsFull() {
        var task = VolunteerTaskMother.defaultTask();
        task.setMaxVolunteers(1);
        var existingAssignment = TaskAssignmentMother.defaultAssignment();
        existingAssignment.setStatus(AssignmentStatus.ACCEPTED);
        var request = new CreateTaskAssignmentRequest(20L, 1L);

        when(volunteerTaskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findByVolunteerIdAndTaskId(20L, 1L)).thenReturn(Optional.empty());
        when(taskAssignmentRepository.findByVolunteerId(20L)).thenReturn(List.of());
        when(taskAssignmentRepository.findByTaskId(1L)).thenReturn(List.of(existingAssignment));

        assertThatThrownBy(() -> taskAssignmentService.createAssignment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximum");
    }

    @Test
    @DisplayName("Should throw exception when creating assignment with non-existing task")
    void shouldThrowExceptionWhenCreatingAssignmentWithNonExistingTask() {
        var request = new CreateTaskAssignmentRequest(10L, 99L);
        when(volunteerTaskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskAssignmentService.createAssignment(request))
                .isInstanceOf(VolunteerTaskNotFoundException.class);
    }

    @Test
    @DisplayName("Volunteer should be able to refuse assignment")
    void volunteerShouldRefuseAssignment() {
        var existing = TaskAssignmentMother.defaultAssignment();
        var request = new VolunteerAssignmentResponseRequest(10L, AssignmentStatus.REFUSED, "Je ne suis pas disponible");

        when(taskAssignmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenReturn(existing);

        var result = taskAssignmentService.respondToAssignment(1L, request);

        assertThat(result.getStatus()).isEqualTo(AssignmentStatus.REFUSED);
        assertThat(result.getComment()).isEqualTo("Je ne suis pas disponible");
        verify(taskAssignmentRepository).save(existing);
    }

    @Test
    @DisplayName("Should reject volunteer response for another volunteer")
    void shouldRejectVolunteerResponseForAnotherVolunteer() {
        var existing = TaskAssignmentMother.defaultAssignment();
        var request = new VolunteerAssignmentResponseRequest(99L, AssignmentStatus.REFUSED, null);

        when(taskAssignmentRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> taskAssignmentService.respondToAssignment(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own assignment");
    }

    @Test
    @DisplayName("Should reject volunteer response with pending status")
    void shouldRejectVolunteerResponseWithPendingStatus() {
        var existing = TaskAssignmentMother.defaultAssignment();
        var request = new VolunteerAssignmentResponseRequest(10L, AssignmentStatus.PENDING, null);

        when(taskAssignmentRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> taskAssignmentService.respondToAssignment(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ACCEPTED or REFUSED");
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
