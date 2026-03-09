package com.moveit.notification.entity;

public enum NotificationType {
    ASSIGNMENT("Assignment notifications for commissaires and volunteers"),
    RESULT("Result publication notifications"),
    SCHEDULE_CHANGE("Schedule change notifications"),
    REGISTRATION("Registration confirmation notifications"),
    CANCELLATION("Cancellation notifications"),
    REMINDER("Reminder notifications"),
    START("Start notifications for trial, competition or championship"),
    ALERT("Urgent alerts");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
