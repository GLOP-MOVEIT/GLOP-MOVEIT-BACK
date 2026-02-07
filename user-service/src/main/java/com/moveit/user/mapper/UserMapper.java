package com.moveit.user.mapper;

import com.moveit.user.dto.User;
import com.moveit.user.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserEntity toEntity(User user);
    User toDto(UserEntity userEntity);
}
