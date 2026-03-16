package com.moveit.volunteer_service.controller;

import org.springframework.web.bind.annotation.*;
import com.moveit.volunteer_service.dto.CreateVolunteerTaskRequest;
import com.moveit.volunteer_service.dto.VolunteerTaskDTO;
import com.moveit.volunteer_service.enums.TaskTargetType;
import com.moveit.volunteer_service.mapper.VolunteerTaskMapper;
import com.moveit.volunteer_service.service.VolunteerTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/volunteer/tasks")
public class VolunteerTaskController {

    private final VolunteerTaskService volunteerTaskService;

    @GetMapping
    public ResponseEntity<List<VolunteerTaskDTO>> getAllTasks() {
        return ResponseEntity.ok(
                VolunteerTaskMapper.toDTOList(volunteerTaskService.getAllTasks()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VolunteerTaskDTO> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(
                VolunteerTaskMapper.toDTO(volunteerTaskService.getTaskById(id)));
    }

    @GetMapping("/target/{targetType}/{targetId}")
    public ResponseEntity<List<VolunteerTaskDTO>> getTasksByTarget(
            @PathVariable TaskTargetType targetType,
            @PathVariable Long targetId) {
        return ResponseEntity.ok(
                VolunteerTaskMapper.toDTOList(volunteerTaskService.getTasksByTarget(targetType, targetId)));
    }

    @GetMapping("/type/{taskTypeId}")
    public ResponseEntity<List<VolunteerTaskDTO>> getTasksByTaskTypeId(
            @PathVariable Long taskTypeId) {
        return ResponseEntity.ok(
                VolunteerTaskMapper.toDTOList(volunteerTaskService.getTasksByTaskTypeId(taskTypeId)));
    }

    @PostMapping
    public ResponseEntity<VolunteerTaskDTO> createTask(
            @Valid @RequestBody CreateVolunteerTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                VolunteerTaskMapper.toDTO(volunteerTaskService.createTask(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VolunteerTaskDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody CreateVolunteerTaskRequest request) {
        return ResponseEntity.ok(
                VolunteerTaskMapper.toDTO(volunteerTaskService.updateTask(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        volunteerTaskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
