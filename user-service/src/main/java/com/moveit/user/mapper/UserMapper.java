package com.moveit.user.mapper;

import com.moveit.user.dto.User;
import com.moveit.user.dto.UserRequest;
import com.moveit.user.entity.RoleEntity;
import com.moveit.user.entity.UserEntity;
import com.moveit.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
@RequiredArgsConstructor
public abstract class UserMapper {

    private final RoleRepository roleRepository;

    public abstract User toDto(UserEntity userEntity);

    @Mapping(target = "role", expression = "java(getSpectatorRole())")
    public abstract UserEntity toEntity(UserRequest userRequest);

    private RoleEntity getSpectatorRole() {
        return this.roleRepository.findByName("SPECTATOR").get();
    }
}
