package com.moveit.user.mapper;

import com.moveit.user.dto.User;
import com.moveit.user.dto.UserRequest;
import com.moveit.user.entity.RoleEntity;
import com.moveit.user.entity.UserEntity;
import com.moveit.user.service.RoleService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    protected RoleService roleService;

    public abstract User toDto(UserEntity userEntity);

    public abstract UserEntity toEntity(User user);

    @Mapping(target = "role", expression = "java(getSpectatorRole())")
    public abstract UserEntity toEntity(UserRequest userRequest);

    protected RoleEntity getSpectatorRole() {
        return roleService.getRoleEntityByName("SPECTATOR");
    }
}
