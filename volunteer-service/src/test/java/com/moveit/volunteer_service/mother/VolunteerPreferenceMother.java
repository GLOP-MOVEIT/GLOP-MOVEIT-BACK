package com.moveit.volunteer_service.mother;

import com.moveit.volunteer_service.entity.VolunteerPreference;

import java.time.LocalDateTime;

public class VolunteerPreferenceMother {

    private VolunteerPreferenceMother() {}

    public static VolunteerPreference defaultPreference() {
        VolunteerPreference preference = new VolunteerPreference();
        preference.setId(1L);
        preference.setUserId(10L);
        preference.setTaskType(VolunteerTaskTypeMother.defaultTaskType());
        preference.setPreferenceOrder(1);
        preference.setCreatedAt(LocalDateTime.now());
        preference.setUpdatedAt(LocalDateTime.now());
        return preference;
    }
}
