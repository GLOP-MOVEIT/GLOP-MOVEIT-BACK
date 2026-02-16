package com.moveit.user.controller;

import com.moveit.user.dto.Role;
import com.moveit.user.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
@Tag(name = "Role", description = "API de gestion des rôles")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Récupérer tous les rôles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rôles récupérés avec succès", content = @Content(schema = @Schema(implementation = Role.class))),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping
    public List<Role> getAllRoles() {
        return this.roleService.getAllRole();
    }
}