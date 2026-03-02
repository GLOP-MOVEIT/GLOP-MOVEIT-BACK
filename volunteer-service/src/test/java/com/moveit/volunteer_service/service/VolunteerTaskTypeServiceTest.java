package com.moveit.volunteer_service.service;

import com.moveit.volunteer_service.dto.CreateVolunteerTaskTypeRequest;
import com.moveit.volunteer_service.entity.VolunteerTaskType;
import com.moveit.volunteer_service.exception.VolunteerTaskTypeNotFoundException;
import com.moveit.volunteer_service.mother.VolunteerTaskTypeMother;
import com.moveit.volunteer_service.repository.VolunteerTaskTypeRepository;
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
class VolunteerTaskTypeServiceTest {

    @InjectMocks
    private VolunteerTaskTypeService volunteerTaskTypeService;

    @Mock
    private VolunteerTaskTypeRepository volunteerTaskTypeRepository;

    @Test
    @DisplayName("Should retrieve all task types")
    void shouldGetAllTaskTypes() {
        var taskType = VolunteerTaskTypeMother.defaultTaskType();
        when(volunteerTaskTypeRepository.findAll()).thenReturn(List.of(taskType));

        var result = volunteerTaskTypeService.getAllTaskTypes();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Accueil");
    }

    @Test
    @DisplayName("Should retrieve task type by id")
    void shouldGetTaskTypeById() {
        var taskType = VolunteerTaskTypeMother.defaultTaskType();
        when(volunteerTaskTypeRepository.findById(1L)).thenReturn(Optional.of(taskType));

        var result = volunteerTaskTypeService.getTaskTypeById(1L);

        assertThat(result.getName()).isEqualTo("Accueil");
    }

    @Test
    @DisplayName("Should throw exception when task type not found")
    void shouldThrowExceptionWhenTaskTypeNotFound() {
        when(volunteerTaskTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerTaskTypeService.getTaskTypeById(99L))
                .isInstanceOf(VolunteerTaskTypeNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should create a task type")
    void shouldCreateTaskType() {
        var request = new CreateVolunteerTaskTypeRequest("Sécurité", "Gestion de la sécurité");
        var saved = VolunteerTaskTypeMother.taskType(2L, "Sécurité", "Gestion de la sécurité");
        when(volunteerTaskTypeRepository.save(any(VolunteerTaskType.class))).thenReturn(saved);

        var result = volunteerTaskTypeService.createTaskType(request);

        assertThat(result.getName()).isEqualTo("Sécurité");
        verify(volunteerTaskTypeRepository).save(any(VolunteerTaskType.class));
    }

    @Test
    @DisplayName("Should update a task type")
    void shouldUpdateTaskType() {
        var existing = VolunteerTaskTypeMother.defaultTaskType();
        var request = new CreateVolunteerTaskTypeRequest("Accueil VIP", "Accueil des VIP");
        when(volunteerTaskTypeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(volunteerTaskTypeRepository.save(any(VolunteerTaskType.class))).thenReturn(existing);

        var result = volunteerTaskTypeService.updateTaskType(1L, request);

        assertThat(result.getName()).isEqualTo("Accueil VIP");
        verify(volunteerTaskTypeRepository).save(existing);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existing task type")
    void shouldThrowExceptionWhenUpdatingNonExistingTaskType() {
        var request = new CreateVolunteerTaskTypeRequest("Test", "Test");
        when(volunteerTaskTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerTaskTypeService.updateTaskType(99L, request))
                .isInstanceOf(VolunteerTaskTypeNotFoundException.class);
    }

    @Test
    @DisplayName("Should delete a task type")
    void shouldDeleteTaskType() {
        when(volunteerTaskTypeRepository.existsById(1L)).thenReturn(true);

        volunteerTaskTypeService.deleteTaskType(1L);

        verify(volunteerTaskTypeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existing task type")
    void shouldThrowExceptionWhenDeletingNonExistingTaskType() {
        when(volunteerTaskTypeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> volunteerTaskTypeService.deleteTaskType(99L))
                .isInstanceOf(VolunteerTaskTypeNotFoundException.class);
    }
}
