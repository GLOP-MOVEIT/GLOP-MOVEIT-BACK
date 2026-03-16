package com.moveit.volunteer_service.mother;

import com.moveit.volunteer_service.entity.TaskAssignment;
import com.moveit.volunteer_service.enums.AssignmentStatus;

import java.time.LocalDateTime;

public class TaskAssignmentMother {

    private TaskAssignmentMother() {}

    public static TaskAssignment defaultAssignment() {
        TaskAssignment assignment = new TaskAssignment();
        assignment.setId(1L);
        assignment.setVolunteerId(10L);
        assignment.setTask(VolunteerTaskMother.defaultTask());
        assignment.setStatus(AssignmentStatus.PENDING);
        assignment.setComment(null);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        return assignment;
    }
}
