package com.moveit.volunteer_service.mother;

import com.moveit.volunteer_service.entity.VolunteerTask;
import com.moveit.volunteer_service.entity.VolunteerTaskType;
import com.moveit.volunteer_service.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.HashSet;

public class VolunteerTaskMother {

    private VolunteerTaskMother() {}

    public static VolunteerTask defaultTask() {
        VolunteerTask task = new VolunteerTask();
        task.setId(1L);
        task.setChampionshipId(1L);
        task.setTitle("Bénévolat accueil");
        task.setDescription("Accueillir les participants à l'entrée");
        task.setTaskType(VolunteerTaskTypeMother.defaultTaskType());
        task.setStatus(TaskStatus.PENDING);
        task.setStartDate(LocalDateTime.of(2026, 6, 1, 8, 0));
        task.setEndDate(LocalDateTime.of(2026, 6, 1, 12, 0));
        task.setMaxVolunteers(5);
        task.setAssignedVolunteerIds(new HashSet<>());
        task.setLocation("Entrée principale");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }

    public static VolunteerTask taskWithType(VolunteerTaskType taskType) {
        VolunteerTask task = defaultTask();
        task.setTaskType(taskType);
        return task;
    }
}
