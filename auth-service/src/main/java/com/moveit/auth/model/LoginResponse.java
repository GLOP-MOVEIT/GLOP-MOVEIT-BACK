package com.moveit.auth.model;

import com.moveit.auth.dto.UserDto;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginResponse {
    private String token;
    private long expiresIn;
    private UserDto user;
}