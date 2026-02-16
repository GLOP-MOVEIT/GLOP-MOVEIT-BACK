package com.moveit.auth.dto;

public record CreateSpectatorRequest(
        String firstName,
        String surname,
        String email,
        String phoneNumber,
        String language,
        Boolean acceptsNotifications,
        Boolean acceptsLocationSharing
) {}
