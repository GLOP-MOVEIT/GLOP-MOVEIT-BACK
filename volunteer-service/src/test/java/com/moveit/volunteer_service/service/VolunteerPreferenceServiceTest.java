package com.moveit.volunteer_service.service;

import com.moveit.volunteer_service.dto.CreateVolunteerPreferenceRequest;
import com.moveit.volunteer_service.entity.VolunteerTaskType;
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
        when(volunteerPreferenceRepository.findByUserIdOrderByPreferenceOrder(10L))
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
        var request = new CreateVolunteerPreferenceRequest(10L, 1L, 1);
        var saved = VolunteerPreferenceMother.defaultPreference();

        when(volunteerTaskTypeRepository.findById(1L)).thenReturn(Optional.of(taskType));
        when(volunteerPreferenceRepository.findByUserIdAndTaskType_Id(10L, 1L)).thenReturn(Optional.empty());
        when(volunteerPreferenceRepository.findByUserIdAndPreferenceOrder(10L, 1)).thenReturn(Optional.empty());
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
        var request = new CreateVolunteerPreferenceRequest(10L, 1L, 1);

        when(volunteerTaskTypeRepository.findById(1L)).thenReturn(Optional.of(taskType));
        when(volunteerPreferenceRepository.findByUserIdAndTaskType_Id(10L, 1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> volunteerPreferenceService.createPreference(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate preference order")
    void shouldThrowExceptionWhenCreatingDuplicatePreferenceOrder() {
        var taskType = VolunteerTaskTypeMother.defaultTaskType();
        var existing = VolunteerPreferenceMother.defaultPreference();
        var request = new CreateVolunteerPreferenceRequest(10L, 2L, 1);

        when(volunteerTaskTypeRepository.findById(2L)).thenReturn(Optional.of(taskType));
        when(volunteerPreferenceRepository.findByUserIdAndTaskType_Id(10L, 2L)).thenReturn(Optional.empty());
        when(volunteerPreferenceRepository.findByUserIdAndPreferenceOrder(10L, 1)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> volunteerPreferenceService.createPreference(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Preference order");
    }

    @Test
    @DisplayName("Should throw exception when creating preference with non-existing task type")
    void shouldThrowExceptionWhenCreatingPreferenceWithNonExistingType() {
        var request = new CreateVolunteerPreferenceRequest(10L, 99L, 1);
        when(volunteerTaskTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerPreferenceService.createPreference(request))
                .isInstanceOf(VolunteerTaskTypeNotFoundException.class);
    }

    @Test
    @DisplayName("Should update a preference")
    void shouldUpdatePreference() {
        var existing = VolunteerPreferenceMother.defaultPreference();
        var taskType = VolunteerTaskTypeMother.defaultTaskType();
        var request = new CreateVolunteerPreferenceRequest(10L, 1L, 2);

        when(volunteerPreferenceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(volunteerTaskTypeRepository.findById(1L)).thenReturn(Optional.of(taskType));
        when(volunteerPreferenceRepository.findByUserIdAndPreferenceOrderAndIdNot(10L, 2, 1L)).thenReturn(Optional.empty());
        when(volunteerPreferenceRepository.save(any(VolunteerPreference.class))).thenReturn(existing);

        var result = volunteerPreferenceService.updatePreference(1L, request);

        assertThat(result.getPreferenceOrder()).isEqualTo(2);
        verify(volunteerPreferenceRepository).save(existing);
    }

    @Test
    @DisplayName("Should return volunteers with preference first")
    void shouldCheckVolunteerPreferences() {
        VolunteerTaskType taskType = VolunteerTaskTypeMother.defaultTaskType();
        VolunteerPreference preferred = VolunteerPreferenceMother.defaultPreference();
        preferred.setUserId(20L);
        VolunteerPreference preferredTwo = VolunteerPreferenceMother.defaultPreference();
        preferredTwo.setId(2L);
        preferredTwo.setUserId(30L);

        when(volunteerTaskTypeRepository.findById(1L)).thenReturn(Optional.of(taskType));
        when(volunteerPreferenceRepository.findByTaskType_IdAndUserIdIn(1L, List.of(10L, 20L, 30L)))
                .thenReturn(List.of(preferred, preferredTwo));

        var result = volunteerPreferenceService.checkVolunteerPreferences(1L, List.of(10L, 20L, 30L));

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getVolunteerId()).isEqualTo(20L);
        assertThat(result.get(0).getHasPreference()).isTrue();
        assertThat(result.get(1).getVolunteerId()).isEqualTo(30L);
        assertThat(result.get(1).getHasPreference()).isTrue();
        assertThat(result.get(2).getVolunteerId()).isEqualTo(10L);
        assertThat(result.get(2).getHasPreference()).isFalse();
    }

    @Test
    @DisplayName("Should ignore duplicate volunteer ids when checking preferences")
    void shouldIgnoreDuplicateVolunteerIdsWhenCheckingPreferences() {
        VolunteerTaskType taskType = VolunteerTaskTypeMother.defaultTaskType();
        VolunteerPreference preferred = VolunteerPreferenceMother.defaultPreference();

        when(volunteerTaskTypeRepository.findById(1L)).thenReturn(Optional.of(taskType));
        when(volunteerPreferenceRepository.findByTaskType_IdAndUserIdIn(1L, List.of(10L, 20L)))
                .thenReturn(List.of(preferred));

        var result = volunteerPreferenceService.checkVolunteerPreferences(1L, List.of(10L, 10L, 20L));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getVolunteerId()).isEqualTo(10L);
        assertThat(result.get(0).getHasPreference()).isTrue();
        assertThat(result.get(1).getVolunteerId()).isEqualTo(20L);
        assertThat(result.get(1).getHasPreference()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when updating with duplicate preference order")
    void shouldThrowExceptionWhenUpdatingWithDuplicatePreferenceOrder() {
        var existing = VolunteerPreferenceMother.defaultPreference();
        var taskType = VolunteerTaskTypeMother.defaultTaskType();
        var other = VolunteerPreferenceMother.defaultPreference();
        other.setId(2L);
        var request = new CreateVolunteerPreferenceRequest(10L, 1L, 2);

        when(volunteerPreferenceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(volunteerTaskTypeRepository.findById(1L)).thenReturn(Optional.of(taskType));
        when(volunteerPreferenceRepository.findByUserIdAndPreferenceOrderAndIdNot(10L, 2, 1L))
                .thenReturn(Optional.of(other));

        assertThatThrownBy(() -> volunteerPreferenceService.updatePreference(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Preference order");
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
