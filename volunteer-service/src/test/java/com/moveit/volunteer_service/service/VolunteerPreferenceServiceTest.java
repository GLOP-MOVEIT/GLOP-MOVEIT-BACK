package com.moveit.volunteer_service.service;

import com.moveit.volunteer_service.dto.CreateVolunteerPreferenceRequest;
import com.moveit.volunteer_service.entity.VolunteerPreference;
import com.moveit.volunteer_service.exception.VolunteerPreferenceNotFoundException;
import com.moveit.volunteer_service.exception.VolunteerTaskTypeNotFoundException;
import com.moveit.volunteer_service.mother.VolunteerPreferenceMother;
import com.moveit.volunteer_service.mother.VolunteerTaskTypeMother;
import com.moveit.volunteer_service.repository.VolunteerPreferenceRepository;
import com.moveit.volunteer_service.repository.VolunteerTaskTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class VolunteerPreferenceServiceTest {

    @InjectMocks
    private VolunteerPreferenceService volunteerPreferenceService;

    @Mock
    private VolunteerPreferenceRepository volunteerPreferenceRepository;

    @Mock
    private VolunteerTaskTypeRepository volunteerTaskTypeRepository;

    @Test
    @DisplayName("Should retrieve preferences by user id")
    void shouldGetPreferencesByUserId() {
        var preference = VolunteerPreferenceMother.defaultPreference();
        when(volunteerPreferenceRepository.findByUserId(10L))
                .thenReturn(List.of(preference));

        var result = volunteerPreferenceService.getPreferencesByUserId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should retrieve preference by id")
    void shouldGetPreferenceById() {
        var preference = VolunteerPreferenceMother.defaultPreference();
        when(volunteerPreferenceRepository.findById(1L)).thenReturn(Optional.of(preference));

        var result = volunteerPreferenceService.getPreferenceById(1L);

        assertThat(result.getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should throw exception when preference not found")
    void shouldThrowExceptionWhenPreferenceNotFound() {
        when(volunteerPreferenceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerPreferenceService.getPreferenceById(99L))
                .isInstanceOf(VolunteerPreferenceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should create a preference")
    void shouldCreatePreference() {
        var taskType = VolunteerTaskTypeMother.defaultTaskType();
        var request = new CreateVolunteerPreferenceRequest(10L, 1L);
        var saved = VolunteerPreferenceMother.defaultPreference();

        when(volunteerTaskTypeRepository.findById(1L)).thenReturn(Optional.of(taskType));
        when(volunteerPreferenceRepository.findByUserIdAndTaskType_Id(10L, 1L)).thenReturn(Optional.empty());
        when(volunteerPreferenceRepository.save(any(VolunteerPreference.class))).thenReturn(saved);

        var result = volunteerPreferenceService.createPreference(request);

        assertThat(result).isNotNull();
        verify(volunteerPreferenceRepository).save(any(VolunteerPreference.class));
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate preference")
    void shouldThrowExceptionWhenCreatingDuplicatePreference() {
        var taskType = VolunteerTaskTypeMother.defaultTaskType();
        var existing = VolunteerPreferenceMother.defaultPreference();
        var request = new CreateVolunteerPreferenceRequest(10L, 1L);

        when(volunteerTaskTypeRepository.findById(1L)).thenReturn(Optional.of(taskType));
        when(volunteerPreferenceRepository.findByUserIdAndTaskType_Id(10L, 1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> volunteerPreferenceService.createPreference(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should throw exception when creating preference with non-existing task type")
    void shouldThrowExceptionWhenCreatingPreferenceWithNonExistingType() {
        var request = new CreateVolunteerPreferenceRequest(10L, 99L);
        when(volunteerTaskTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerPreferenceService.createPreference(request))
                .isInstanceOf(VolunteerTaskTypeNotFoundException.class);
    }

    @Test
    @DisplayName("Should update a preference")
    void shouldUpdatePreference() {
        var existing = VolunteerPreferenceMother.defaultPreference();
        var taskType = VolunteerTaskTypeMother.defaultTaskType();
        var request = new CreateVolunteerPreferenceRequest(10L, 1L);

        when(volunteerPreferenceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(volunteerTaskTypeRepository.findById(1L)).thenReturn(Optional.of(taskType));
        when(volunteerPreferenceRepository.save(any(VolunteerPreference.class))).thenReturn(existing);

        var result = volunteerPreferenceService.updatePreference(1L, request);

        assertThat(result.getTaskType().getId()).isEqualTo(1L);
        verify(volunteerPreferenceRepository).save(existing);
    }

    @Test
    @DisplayName("Should delete a preference")
    void shouldDeletePreference() {
        when(volunteerPreferenceRepository.existsById(1L)).thenReturn(true);

        volunteerPreferenceService.deletePreference(1L);

        verify(volunteerPreferenceRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existing preference")
    void shouldThrowExceptionWhenDeletingNonExistingPreference() {
        when(volunteerPreferenceRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> volunteerPreferenceService.deletePreference(99L))
                .isInstanceOf(VolunteerPreferenceNotFoundException.class);
    }
}
