package com.moveit.auth.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserDto {
    private Integer id;
    private String nickname;
    private Date lastConnectionDate;
}
