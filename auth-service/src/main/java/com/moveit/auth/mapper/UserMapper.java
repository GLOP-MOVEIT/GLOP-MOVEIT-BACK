package com.moveit.auth.mapper;

import com.moveit.auth.dto.UserDto;
import com.moveit.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", source = "role.name")
    UserDto toDto(User user);
}
