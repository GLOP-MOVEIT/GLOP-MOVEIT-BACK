package com.moveit.auth.dto;

public record RegisterUserDto(
        String nickname,
        String password,
        String firstName,
        String surname,
        String email,
        String phoneNumber,
        String language,
        Boolean acceptsNotifications,
        Boolean acceptsLocationSharing
) {}