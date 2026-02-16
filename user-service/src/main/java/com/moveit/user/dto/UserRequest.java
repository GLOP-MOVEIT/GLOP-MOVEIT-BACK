package com.moveit.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    private Integer userId;
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Surname is required")
    private String surname;
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "phoneNumber is required")
    private String phoneNumber;
    @NotBlank(message = "Language is required")
    private String language;
    @NotNull(message = "Accepts notifications is required")
    private Boolean acceptsNotifications;
    @NotNull(message = "Accepts location sharing is required")
    private Boolean acceptsLocationSharing;
}