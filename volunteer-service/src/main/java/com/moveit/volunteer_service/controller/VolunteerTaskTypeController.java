package com.moveit.volunteer_service.controller;

import org.springframework.web.bind.annotation.*;
import com.moveit.volunteer_service.dto.CreateVolunteerTaskTypeRequest;
import com.moveit.volunteer_service.dto.VolunteerTaskTypeDTO;
import com.moveit.volunteer_service.mapper.VolunteerTaskTypeMapper;
import com.moveit.volunteer_service.service.VolunteerTaskTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/volunteer/task-types")
public class VolunteerTaskTypeController {

    private final VolunteerTaskTypeService volunteerTaskTypeService;

    @GetMapping
    public ResponseEntity<List<VolunteerTaskTypeDTO>> getAllTaskTypes() {
        return ResponseEntity.ok(
                VolunteerTaskTypeMapper.toDTOList(volunteerTaskTypeService.getAllTaskTypes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VolunteerTaskTypeDTO> getTaskTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(
                VolunteerTaskTypeMapper.toDTO(volunteerTaskTypeService.getTaskTypeById(id)));
    }

    @PostMapping
    public ResponseEntity<VolunteerTaskTypeDTO> createTaskType(
            @Valid @RequestBody CreateVolunteerTaskTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                VolunteerTaskTypeMapper.toDTO(volunteerTaskTypeService.createTaskType(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VolunteerTaskTypeDTO> updateTaskType(
            @PathVariable Long id,
            @Valid @RequestBody CreateVolunteerTaskTypeRequest request) {
        return ResponseEntity.ok(
                VolunteerTaskTypeMapper.toDTO(volunteerTaskTypeService.updateTaskType(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskType(@PathVariable Long id) {
        volunteerTaskTypeService.deleteTaskType(id);
        return ResponseEntity.noContent().build();
    }
}
