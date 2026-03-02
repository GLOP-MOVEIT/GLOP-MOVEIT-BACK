package com.moveit.volunteer_service.controller;

import com.moveit.volunteer_service.dto.CreateTaskAssignmentRequest;
import com.moveit.volunteer_service.dto.TaskAssignmentDTO;
import com.moveit.volunteer_service.dto.UpdateTaskAssignmentStatusRequest;
import com.moveit.volunteer_service.enums.AssignmentStatus;
import com.moveit.volunteer_service.mapper.TaskAssignmentMapper;
import com.moveit.volunteer_service.service.TaskAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/volunteer/assignments")
public class TaskAssignmentController {

    private final TaskAssignmentService taskAssignmentService;

    @GetMapping("/volunteer/{volunteerId}")
    public ResponseEntity<List<TaskAssignmentDTO>> getAssignmentsByVolunteerId(
            @PathVariable Long volunteerId) {
        return ResponseEntity.ok(
                TaskAssignmentMapper.toDTOList(taskAssignmentService.getAssignmentsByVolunteerId(volunteerId)));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TaskAssignmentDTO>> getAssignmentsByTaskId(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(
                TaskAssignmentMapper.toDTOList(taskAssignmentService.getAssignmentsByTaskId(taskId)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskAssignmentDTO>> getAssignmentsByStatus(
            @PathVariable AssignmentStatus status) {
        return ResponseEntity.ok(
                TaskAssignmentMapper.toDTOList(taskAssignmentService.getAssignmentsByStatus(status)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskAssignmentDTO> getAssignmentById(@PathVariable Long id) {
        return ResponseEntity.ok(
                TaskAssignmentMapper.toDTO(taskAssignmentService.getAssignmentById(id)));
    }

    @PostMapping
    public ResponseEntity<TaskAssignmentDTO> createAssignment(
            @Valid @RequestBody CreateTaskAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                TaskAssignmentMapper.toDTO(taskAssignmentService.createAssignment(request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskAssignmentDTO> updateAssignmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskAssignmentStatusRequest request) {
        return ResponseEntity.ok(
                TaskAssignmentMapper.toDTO(taskAssignmentService.updateAssignmentStatus(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        taskAssignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
}
