package com.moveit.auth.model;

public record RegisterUserDto(
        String email,
        String password,
        String firstName,
        String surname,
        boolean acceptsNotifications,
        boolean acceptsLocation
) {}