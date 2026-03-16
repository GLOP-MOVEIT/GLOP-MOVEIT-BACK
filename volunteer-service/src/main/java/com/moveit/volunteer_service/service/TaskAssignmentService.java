package com.moveit.volunteer_service.service;

import com.moveit.volunteer_service.dto.CreateTaskAssignmentRequest;
import com.moveit.volunteer_service.dto.VolunteerWithPreferenceDTO;
import com.moveit.volunteer_service.dto.VolunteerAssignmentResponseRequest;
import com.moveit.volunteer_service.entity.TaskAssignment;
import com.moveit.volunteer_service.entity.VolunteerPreference;
import com.moveit.volunteer_service.entity.VolunteerTask;
import com.moveit.volunteer_service.enums.AssignmentStatus;
import com.moveit.volunteer_service.exception.TaskAssignmentNotFoundException;
import com.moveit.volunteer_service.exception.VolunteerTaskNotFoundException;
import com.moveit.volunteer_service.repository.TaskAssignmentRepository;
import com.moveit.volunteer_service.repository.VolunteerPreferenceRepository;
import com.moveit.volunteer_service.repository.VolunteerTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TaskAssignmentService {

    private final TaskAssignmentRepository taskAssignmentRepository;
    private final VolunteerTaskRepository volunteerTaskRepository;
    private final VolunteerPreferenceRepository volunteerPreferenceRepository;

    public List<TaskAssignment> getAssignmentsByVolunteerId(Long volunteerId) {
        return taskAssignmentRepository.findByVolunteerId(volunteerId);
    }

    public List<TaskAssignment> getAssignmentsByTaskId(Long taskId) {
        return taskAssignmentRepository.findByTaskId(taskId);
    }

    public List<Long> getAvailableVolunteersForTask(Long taskId, List<Long> volunteerIds) {
        VolunteerTask task = loadTaskOrThrow(taskId);
        validateTaskDatesForAvailability(task);

        List<Long> orderedVolunteerIds = normalizeVolunteerIds(volunteerIds);
        if (orderedVolunteerIds.isEmpty()) {
            return List.of();
        }

        Set<Long> busyVolunteerIds = findBusyVolunteerIds(task, taskId, orderedVolunteerIds);
        return orderedVolunteerIds.stream()
                .filter(volunteerId -> !busyVolunteerIds.contains(volunteerId))
                .toList();
    }

    public List<VolunteerWithPreferenceDTO> getAvailableVolunteersWithPreferenceForTask(Long taskId, List<Long> volunteerIds) {
        VolunteerTask task = loadTaskOrThrow(taskId);

        List<Long> availableVolunteerIds = getAvailableVolunteersForTask(taskId, volunteerIds);
        if (availableVolunteerIds.isEmpty()) {
            return List.of();
        }

        Set<Long> preferredVolunteerIds = findPreferredVolunteerIds(task.getTaskType().getId(), availableVolunteerIds);

        List<VolunteerWithPreferenceDTO> preferred = availableVolunteerIds.stream()
                .filter(preferredVolunteerIds::contains)
                .map(volunteerId -> new VolunteerWithPreferenceDTO(volunteerId, true))
                .toList();

        List<VolunteerWithPreferenceDTO> others = availableVolunteerIds.stream()
                .filter(volunteerId -> !preferredVolunteerIds.contains(volunteerId))
                .map(volunteerId -> new VolunteerWithPreferenceDTO(volunteerId, false))
                .toList();

        return Stream.concat(preferred.stream(), others.stream()).toList();
    }

    public List<TaskAssignment> getAssignmentsByStatus(AssignmentStatus status) {
        return taskAssignmentRepository.findByStatus(status);
    }

    public TaskAssignment getAssignmentById(Long id) {
        return taskAssignmentRepository.findById(id)
                .orElseThrow(() -> new TaskAssignmentNotFoundException(id));
    }

    public TaskAssignment createAssignment(CreateTaskAssignmentRequest request) {
        VolunteerTask task = loadTaskOrThrow(request.getTaskId());

        assertNoDuplicateAssignment(request.getVolunteerId(), request.getTaskId());
        assertNoScheduleConflict(request.getVolunteerId(), task);
        assertTaskCapacity(request.getTaskId(), task.getMaxVolunteers());

        return taskAssignmentRepository.save(buildPendingAssignment(request.getVolunteerId(), task));
    }

    public TaskAssignment respondToAssignment(Long id, VolunteerAssignmentResponseRequest request) {
        TaskAssignment existing = loadAssignmentOrThrow(id);
        validateResponseRequest(existing, request);

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

    private VolunteerTask loadTaskOrThrow(Long taskId) {
        return volunteerTaskRepository.findById(taskId)
                .orElseThrow(() -> new VolunteerTaskNotFoundException(taskId));
    }

    private TaskAssignment loadAssignmentOrThrow(Long assignmentId) {
        return taskAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new TaskAssignmentNotFoundException(assignmentId));
    }

    private void validateTaskDatesForAvailability(VolunteerTask task) {
        if (task.getStartDate() == null || task.getEndDate() == null) {
            throw new IllegalArgumentException("Task must have startDate and endDate to check volunteer availability");
        }
    }

    private List<Long> normalizeVolunteerIds(List<Long> volunteerIds) {
        return volunteerIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf));
    }

    private Set<Long> findBusyVolunteerIds(VolunteerTask task, Long taskId, List<Long> volunteerIds) {
        return taskAssignmentRepository.findByVolunteerIdIn(volunteerIds)
                .stream()
                .filter(existing -> existing.getStatus() != AssignmentStatus.REFUSED)
                .filter(existing -> existing.getTask() != null && !existing.getTask().getId().equals(taskId))
                .filter(existing -> hasScheduleOverlap(task, existing.getTask()))
                .map(TaskAssignment::getVolunteerId)
                .collect(Collectors.toSet());
    }

    private Set<Long> findPreferredVolunteerIds(Long taskTypeId, List<Long> volunteerIds) {
        return volunteerPreferenceRepository.findByTaskType_IdAndUserIdIn(taskTypeId, volunteerIds)
                .stream()
                .map(VolunteerPreference::getUserId)
                .collect(Collectors.toSet());
    }

    private void assertNoDuplicateAssignment(Long volunteerId, Long taskId) {
        taskAssignmentRepository.findByVolunteerIdAndTaskId(volunteerId, taskId)
                .ifPresent(ignored -> {
                    throw new IllegalArgumentException(
                            "Assignment already exists for volunteer " + volunteerId + " and task " + taskId);
                });
    }

    private void assertNoScheduleConflict(Long volunteerId, VolunteerTask task) {
        boolean hasTimeConflict = taskAssignmentRepository.findByVolunteerId(volunteerId)
                .stream()
                .filter(existing -> existing.getStatus() != AssignmentStatus.REFUSED)
                .map(TaskAssignment::getTask)
                .filter(existingTask -> existingTask != null && !existingTask.getId().equals(task.getId()))
                .anyMatch(existingTask -> hasScheduleOverlap(task, existingTask));

        if (hasTimeConflict) {
            throw new IllegalArgumentException("Volunteer already has another task on this time slot");
        }
    }

    private void assertTaskCapacity(Long taskId, Integer maxVolunteers) {
        if (maxVolunteers == null) {
            return;
        }

        long currentAssignments = taskAssignmentRepository.findByTaskId(taskId)
                .stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REFUSED)
                .count();

        if (currentAssignments >= maxVolunteers) {
            throw new IllegalArgumentException("Task has reached maximum number of volunteers");
        }
    }

    private TaskAssignment buildPendingAssignment(Long volunteerId, VolunteerTask task) {
        TaskAssignment assignment = new TaskAssignment();
        assignment.setVolunteerId(volunteerId);
        assignment.setTask(task);
        assignment.setStatus(AssignmentStatus.PENDING);
        return assignment;
    }

    private void validateResponseRequest(TaskAssignment existing, VolunteerAssignmentResponseRequest request) {
        if (!existing.getVolunteerId().equals(request.getVolunteerId())) {
            throw new IllegalArgumentException("Volunteer can only respond to their own assignment");
        }

        if (request.getStatus() == AssignmentStatus.PENDING) {
            throw new IllegalArgumentException("Volunteer response status must be ACCEPTED or REFUSED");
        }

        if (existing.getStatus() != AssignmentStatus.PENDING) {
            throw new IllegalArgumentException("Assignment has already been responded to");
        }
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
}
