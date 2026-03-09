package com.moveit.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginUserDto(
        @NotBlank(message = "Nickname is required")
        String nickname,
        @NotBlank(message = "Password is required")
        String password
) {}