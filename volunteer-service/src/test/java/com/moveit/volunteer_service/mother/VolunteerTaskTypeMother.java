package com.moveit.volunteer_service.mother;

import com.moveit.volunteer_service.entity.VolunteerTaskType;

public class VolunteerTaskTypeMother {

    private VolunteerTaskTypeMother() {}

    public static VolunteerTaskType defaultTaskType() {
        VolunteerTaskType taskType = new VolunteerTaskType();
        taskType.setId(1L);
        taskType.setName("Accueil");
        taskType.setDescription("Accueil des participants");
        return taskType;
    }

    public static VolunteerTaskType taskType(Long id, String name, String description) {
        VolunteerTaskType taskType = new VolunteerTaskType();
        taskType.setId(id);
        taskType.setName(name);
        taskType.setDescription(description);
        return taskType;
    }
}
