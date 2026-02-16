package com.moveit.user.mapper;

import com.moveit.user.dto.Role;
import com.moveit.user.entity.RoleEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toDto(RoleEntity roleEntity);
    List<Role> toDtoList(List<RoleEntity> roleEntities);
}