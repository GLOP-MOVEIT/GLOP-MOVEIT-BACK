package com.moveit.user.service;

import com.moveit.user.dto.Role;
import com.moveit.user.entity.RoleEntity;
import com.moveit.user.exception.RoleNotFoundException;
import com.moveit.user.mapper.RoleMapper;
import com.moveit.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public List<Role> getAllRole() {
        return roleMapper.toDtoList(roleRepository.findAll());
    }

    public RoleEntity getRoleEntityByName(String name) {
        return this.roleRepository.findByName(name)
                .orElseThrow(() -> new RoleNotFoundException(name));
    }
}