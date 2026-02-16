package com.moveit.user.controller;

import com.moveit.user.dto.User;
import com.moveit.user.dto.UserRequest;
import com.moveit.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User", description = "API de gestion des utilisateurs")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Récupérer tous les utilisateurs avec pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateurs récupérés avec succès", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping
    public Page<User> getAllUsers(Pageable pageable) {
        return this.userService.getAllUsers(pageable);
    }

    @Operation(summary = "Récupérer le profil d'un utilisateur par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil utilisateur récupéré avec succès", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping("/{id}")
    public User getUserProfile(@PathVariable Integer id) {
        return this.userService.getUserById(id);
    }

    @Operation(summary = "Mettre à jour le profil d'un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil utilisateur mis à jour avec succès", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Données de mise à jour invalides", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PutMapping
    public User updateUserProfile(@RequestBody @Valid UserRequest user) {
        return this.userService.updateUser(user);
    }

    @Operation(summary = "Créer un nouveau spectateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Spectateur créé avec succès", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Données de création invalides", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PostMapping
    public User createSpectator(@RequestBody @Valid UserRequest user) {
        return this.userService.createSpectator(user);
    }
}