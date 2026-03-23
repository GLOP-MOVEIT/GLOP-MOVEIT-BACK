package com.moveit.user.mapper;

import com.moveit.user.dto.Request;
import com.moveit.user.entity.RequestEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, RoleMapper.class})
public interface RequestMapper {

    Request toDto(RequestEntity requestEntity);
}
