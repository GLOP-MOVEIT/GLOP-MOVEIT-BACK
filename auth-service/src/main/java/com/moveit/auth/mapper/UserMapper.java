package com.moveit.auth.mapper;

import com.moveit.auth.dto.UserDto;
import com.moveit.auth.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);
}
