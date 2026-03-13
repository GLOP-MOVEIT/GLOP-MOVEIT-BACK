package com.moveit.volunteer_service.controller;

import org.springframework.web.bind.annotation.*;
import com.moveit.volunteer_service.dto.CheckVolunteerPreferencesRequest;
import com.moveit.volunteer_service.dto.CreateVolunteerPreferenceRequest;
import com.moveit.volunteer_service.dto.VolunteerPreferenceDTO;
import com.moveit.volunteer_service.dto.VolunteerWithPreferenceDTO;
import com.moveit.volunteer_service.mapper.VolunteerPreferenceMapper;
import com.moveit.volunteer_service.service.VolunteerPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/volunteer/preferences")
public class VolunteerPreferenceController {

    private final VolunteerPreferenceService volunteerPreferenceService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VolunteerPreferenceDTO>> getPreferencesByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                VolunteerPreferenceMapper.toDTOList(volunteerPreferenceService.getPreferencesByUserId(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VolunteerPreferenceDTO> getPreferenceById(@PathVariable Long id) {
        return ResponseEntity.ok(
                VolunteerPreferenceMapper.toDTO(volunteerPreferenceService.getPreferenceById(id)));
    }

    @PostMapping
    public ResponseEntity<VolunteerPreferenceDTO> createPreference(
            @Valid @RequestBody CreateVolunteerPreferenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                VolunteerPreferenceMapper.toDTO(volunteerPreferenceService.createPreference(request)));
    }

        @PostMapping("/check")
        public ResponseEntity<List<VolunteerWithPreferenceDTO>> checkVolunteerPreferences(
            @Valid @RequestBody CheckVolunteerPreferencesRequest request) {
        return ResponseEntity.ok(
            volunteerPreferenceService.checkVolunteerPreferences(request.getTaskTypeId(), request.getVolunteerIds()));
        }

    @PutMapping("/{id}")
    public ResponseEntity<VolunteerPreferenceDTO> updatePreference(
            @PathVariable Long id,
            @Valid @RequestBody CreateVolunteerPreferenceRequest request) {
        return ResponseEntity.ok(
                VolunteerPreferenceMapper.toDTO(volunteerPreferenceService.updatePreference(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePreference(@PathVariable Long id) {
        volunteerPreferenceService.deletePreference(id);
        return ResponseEntity.noContent().build();
    }
}
