package com.moveit.volunteer_service.service;

import com.moveit.volunteer_service.dto.CreateVolunteerPreferenceRequest;
import com.moveit.volunteer_service.dto.VolunteerWithPreferenceDTO;
import com.moveit.volunteer_service.entity.VolunteerPreference;
import com.moveit.volunteer_service.entity.VolunteerTaskType;
import com.moveit.volunteer_service.exception.VolunteerPreferenceNotFoundException;
import com.moveit.volunteer_service.exception.VolunteerTaskTypeNotFoundException;
import com.moveit.volunteer_service.repository.VolunteerPreferenceRepository;
import com.moveit.volunteer_service.repository.VolunteerTaskTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VolunteerPreferenceService {

    private final VolunteerPreferenceRepository volunteerPreferenceRepository;
    private final VolunteerTaskTypeRepository volunteerTaskTypeRepository;

    public List<VolunteerPreference> getPreferencesByUserId(Long userId) {
        return volunteerPreferenceRepository.findByUserIdOrderByPreferenceOrder(userId);
    }

    public VolunteerPreference getPreferenceById(Long id) {
        return volunteerPreferenceRepository.findById(id)
                .orElseThrow(() -> new VolunteerPreferenceNotFoundException(id));
    }

    public VolunteerPreference createPreference(CreateVolunteerPreferenceRequest request) {
        VolunteerTaskType taskType = volunteerTaskTypeRepository.findById(request.getTaskTypeId())
                .orElseThrow(() -> new VolunteerTaskTypeNotFoundException(request.getTaskTypeId()));

        volunteerPreferenceRepository.findByUserIdAndTaskType_Id(request.getUserId(), request.getTaskTypeId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Preference already exists for user " + request.getUserId() + " and task type " + request.getTaskTypeId());
                });

        volunteerPreferenceRepository.findByUserIdAndPreferenceOrder(request.getUserId(), request.getPreferenceOrder())
            .ifPresent(existing -> {
                throw new IllegalArgumentException(
                    "Preference order " + request.getPreferenceOrder() + " already exists for user " + request.getUserId());
            });

        VolunteerPreference preference = new VolunteerPreference();
        preference.setUserId(request.getUserId());
        preference.setTaskType(taskType);
        preference.setPreferenceOrder(request.getPreferenceOrder());
        return volunteerPreferenceRepository.save(preference);
    }

    public VolunteerPreference updatePreference(Long id, CreateVolunteerPreferenceRequest request) {
        VolunteerPreference existing = volunteerPreferenceRepository.findById(id)
                .orElseThrow(() -> new VolunteerPreferenceNotFoundException(id));
        VolunteerTaskType taskType = volunteerTaskTypeRepository.findById(request.getTaskTypeId())
                .orElseThrow(() -> new VolunteerTaskTypeNotFoundException(request.getTaskTypeId()));

        volunteerPreferenceRepository.findByUserIdAndPreferenceOrderAndIdNot(
                request.getUserId(), request.getPreferenceOrder(), id)
            .ifPresent(other -> {
                throw new IllegalArgumentException(
                    "Preference order " + request.getPreferenceOrder() + " already exists for user " + request.getUserId());
            });

        existing.setUserId(request.getUserId());
        existing.setTaskType(taskType);
        existing.setPreferenceOrder(request.getPreferenceOrder());
        return volunteerPreferenceRepository.save(existing);
    }

    public List<VolunteerWithPreferenceDTO> checkVolunteerPreferences(Long taskTypeId, List<Long> volunteerIds) {
        volunteerTaskTypeRepository.findById(taskTypeId)
                .orElseThrow(() -> new VolunteerTaskTypeNotFoundException(taskTypeId));

        List<Long> orderedVolunteerIds = volunteerIds.stream()
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf));

        Set<Long> preferredVolunteerIds = volunteerPreferenceRepository
                .findByTaskType_IdAndUserIdIn(taskTypeId, orderedVolunteerIds)
                .stream()
                .map(VolunteerPreference::getUserId)
                .collect(java.util.stream.Collectors.toSet());

        List<VolunteerWithPreferenceDTO> preferred = orderedVolunteerIds.stream()
                .filter(preferredVolunteerIds::contains)
                .map(volunteerId -> new VolunteerWithPreferenceDTO(volunteerId, true))
                .toList();

        List<VolunteerWithPreferenceDTO> others = orderedVolunteerIds.stream()
                .filter(volunteerId -> !preferredVolunteerIds.contains(volunteerId))
                .map(volunteerId -> new VolunteerWithPreferenceDTO(volunteerId, false))
                .toList();

        return java.util.stream.Stream.concat(preferred.stream(), others.stream()).toList();
    }

    public void deletePreference(Long id) {
        if (!volunteerPreferenceRepository.existsById(id)) {
            throw new VolunteerPreferenceNotFoundException(id);
        }
        volunteerPreferenceRepository.deleteById(id);
    }
}
