package com.moveit.volunteer_service.service;

import com.moveit.volunteer_service.dto.CreateTaskAssignmentRequest;
import com.moveit.volunteer_service.dto.UpdateTaskAssignmentStatusRequest;
import com.moveit.volunteer_service.entity.TaskAssignment;
import com.moveit.volunteer_service.entity.VolunteerTask;
import com.moveit.volunteer_service.enums.AssignmentStatus;
import com.moveit.volunteer_service.exception.TaskAssignmentNotFoundException;
import com.moveit.volunteer_service.exception.VolunteerTaskNotFoundException;
import com.moveit.volunteer_service.repository.TaskAssignmentRepository;
import com.moveit.volunteer_service.repository.VolunteerTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public TaskAssignment updateAssignmentStatus(Long id, UpdateTaskAssignmentStatusRequest request) {
        TaskAssignment existing = taskAssignmentRepository.findById(id)
                .orElseThrow(() -> new TaskAssignmentNotFoundException(id));
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
