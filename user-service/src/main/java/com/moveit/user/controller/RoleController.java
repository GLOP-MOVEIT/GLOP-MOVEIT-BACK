package com.moveit.user.controller;

import com.moveit.user.dto.Role;
import com.moveit.user.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
@Tag(name = "Role", description = "API de gestion des rôles")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<Role> getAllRoles() {
        return this.roleService.getAllRole();
    }

    @GetMapping("/{name}")
    public Role getRoleByName(@RequestParam String name) {
        return this.roleService.getRoleByName(name);
    }
}