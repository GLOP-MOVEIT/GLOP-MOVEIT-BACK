package com.moveit.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterUserDto(
        @NotBlank(message = "Nickname is required")
        String nickname,
        @NotBlank(message = "Password is required")
        String password,
        @NotBlank(message = "First name is required")
        String firstName,
        @NotBlank(message = "Surname is required")
        String surname,
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Phone number is required")
        String phoneNumber,
        @NotBlank(message = "Language is required")
        String language,
        @NotNull(message = "Accepts notifications is required")
        Boolean acceptsNotifications,
        @NotNull(message = "Accepts location sharing is required")
        Boolean acceptsLocationSharing
) {}