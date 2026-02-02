package com.moveit.auth.mapper;

import com.moveit.auth.dto.UserDto;
import com.moveit.auth.entity.UserAuth;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserAuthMapper {

    UserDto toDto(UserAuth userAuth);
}
