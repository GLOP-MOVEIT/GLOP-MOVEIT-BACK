package com.moveit.volunteer_service.service;

import com.moveit.volunteer_service.dto.CreateTaskAssignmentRequest;
import com.moveit.volunteer_service.dto.UpdateTaskAssignmentStatusRequest;
import com.moveit.volunteer_service.dto.VolunteerAssignmentResponseRequest;
import com.moveit.volunteer_service.entity.TaskAssignment;
import com.moveit.volunteer_service.entity.VolunteerTask;
import com.moveit.volunteer_service.enums.AssignmentStatus;
import com.moveit.volunteer_service.exception.TaskAssignmentNotFoundException;
import com.moveit.volunteer_service.exception.VolunteerTaskNotFoundException;
import com.moveit.volunteer_service.repository.TaskAssignmentRepository;
import com.moveit.volunteer_service.repository.VolunteerTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskAssignmentService {

    private final TaskAssignmentRepository taskAssignmentRepository;
    private final VolunteerTaskRepository volunteerTaskRepository;

    public List<TaskAssignment> getAssignmentsByVolunteerId(Long volunteerId) {
        return taskAssignmentRepository.findByVolunteerId(volunteerId);
    }

    public List<TaskAssignment> getAssignmentsByTaskId(Long taskId) {
        return taskAssignmentRepository.findByTaskId(taskId);
    }

    public List<Long> getAvailableVolunteersForTask(Long taskId, List<Long> volunteerIds) {
        VolunteerTask task = volunteerTaskRepository.findById(taskId)
                .orElseThrow(() -> new VolunteerTaskNotFoundException(taskId));

        if (task.getStartDate() == null || task.getEndDate() == null) {
            throw new IllegalArgumentException("Task must have startDate and endDate to check volunteer availability");
        }

        List<Long> orderedVolunteerIds = volunteerIds.stream()
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf));

        if (orderedVolunteerIds.isEmpty()) {
            return List.of();
        }

        Set<Long> busyVolunteerIds = taskAssignmentRepository.findByVolunteerIdIn(orderedVolunteerIds)
                .stream()
                .filter(existing -> existing.getStatus() != AssignmentStatus.REFUSED)
                .filter(existing -> existing.getTask() != null && !existing.getTask().getId().equals(taskId))
                .filter(existing -> hasScheduleOverlap(task, existing.getTask()))
                .map(TaskAssignment::getVolunteerId)
                .collect(java.util.stream.Collectors.toSet());

        return orderedVolunteerIds.stream()
                .filter(volunteerId -> !busyVolunteerIds.contains(volunteerId))
                .toList();
    }

    public List<TaskAssignment> getAssignmentsByStatus(AssignmentStatus status) {
        return taskAssignmentRepository.findByStatus(status);
    }

    public TaskAssignment getAssignmentById(Long id) {
        return taskAssignmentRepository.findById(id)
                .orElseThrow(() -> new TaskAssignmentNotFoundException(id));
    }

    public TaskAssignment createAssignment(CreateTaskAssignmentRequest request) {
        VolunteerTask task = volunteerTaskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new VolunteerTaskNotFoundException(request.getTaskId()));

        taskAssignmentRepository.findByVolunteerIdAndTaskId(request.getVolunteerId(), request.getTaskId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Assignment already exists for volunteer " + request.getVolunteerId() + " and task " + request.getTaskId());
                });

        boolean hasTimeConflict = taskAssignmentRepository.findByVolunteerId(request.getVolunteerId())
            .stream()
            .filter(existing -> existing.getStatus() != AssignmentStatus.REFUSED)
            .map(TaskAssignment::getTask)
            .filter(existingTask -> existingTask != null && !existingTask.getId().equals(task.getId()))
            .anyMatch(existingTask -> hasScheduleOverlap(task, existingTask));

        if (hasTimeConflict) {
            throw new IllegalArgumentException("Volunteer already has another task on this time slot");
        }

        if (task.getMaxVolunteers() != null) {
            long currentAssignments = taskAssignmentRepository.findByTaskId(request.getTaskId())
                    .stream()
                    .filter(a -> a.getStatus() != AssignmentStatus.REFUSED)
                    .count();
            if (currentAssignments >= task.getMaxVolunteers()) {
                throw new IllegalArgumentException("Task has reached maximum number of volunteers");
            }
        }

        TaskAssignment assignment = new TaskAssignment();
        assignment.setVolunteerId(request.getVolunteerId());
        assignment.setTask(task);
        assignment.setStatus(AssignmentStatus.PENDING);
        assignment.setComment(request.getComment());
        return taskAssignmentRepository.save(assignment);
    }

    private boolean hasScheduleOverlap(VolunteerTask first, VolunteerTask second) {
        LocalDateTime firstStart = first.getStartDate();
        LocalDateTime firstEnd = first.getEndDate();
        LocalDateTime secondStart = second.getStartDate();
        LocalDateTime secondEnd = second.getEndDate();

        // If one task has incomplete dates, we skip overlap validation for that pair.
        if (firstStart == null || firstEnd == null || secondStart == null || secondEnd == null) {
            return false;
        }

        return firstStart.isBefore(secondEnd) && firstEnd.isAfter(secondStart);
    }

    public TaskAssignment updateAssignmentStatus(Long id, UpdateTaskAssignmentStatusRequest request) {
        TaskAssignment existing = taskAssignmentRepository.findById(id)
                .orElseThrow(() -> new TaskAssignmentNotFoundException(id));
        existing.setStatus(request.getStatus());
        if (request.getComment() != null) {
            existing.setComment(request.getComment());
        }
        return taskAssignmentRepository.save(existing);
    }

    public TaskAssignment respondToAssignment(Long id, VolunteerAssignmentResponseRequest request) {
        TaskAssignment existing = taskAssignmentRepository.findById(id)
                .orElseThrow(() -> new TaskAssignmentNotFoundException(id));

        if (!existing.getVolunteerId().equals(request.getVolunteerId())) {
            throw new IllegalArgumentException("Volunteer can only respond to their own assignment");
        }

        if (request.getStatus() == AssignmentStatus.PENDING) {
            throw new IllegalArgumentException("Volunteer response status must be ACCEPTED or REFUSED");
        }

        if (existing.getStatus() != AssignmentStatus.PENDING) {
            throw new IllegalArgumentException("Assignment has already been responded to");
        }

        existing.setStatus(request.getStatus());
        if (request.getComment() != null) {
            existing.setComment(request.getComment());
        }

        return taskAssignmentRepository.save(existing);
    }

    public void deleteAssignment(Long id) {
        if (!taskAssignmentRepository.existsById(id)) {
            throw new TaskAssignmentNotFoundException(id);
        }
        taskAssignmentRepository.deleteById(id);
    }
}
