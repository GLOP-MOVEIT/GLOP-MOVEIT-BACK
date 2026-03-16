package com.moveit.user.controller;

import com.moveit.user.dto.Team;
import com.moveit.user.dto.TeamRequest;
import com.moveit.user.service.TeamService;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
@Tag(name = "Team", description = "API de gestion des équipes")
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "Récupérer toutes les équipes avec pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Équipes récupérées avec succès", content = @Content(schema = @Schema(implementation = Team.class))),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping
    public Page<Team> getAllTeams(Pageable pageable) {
        return this.teamService.getAllTeams(pageable);
    }

    @Operation(summary = "Récupérer toutes les équipes d'un athlète")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Équipes de l'athlète récupérées avec succès", content = @Content(schema = @Schema(implementation = Team.class))),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping("/athletes/{athleteId}")
    public List<Team> getTeamsByAthleteId(@PathVariable Integer athleteId) {
        return this.teamService.getTeamsByAthleteId(athleteId);
    }

    @Operation(summary = "Récupérer une équipe par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Équipe récupérée avec succès", content = @Content(schema = @Schema(implementation = Team.class))),
            @ApiResponse(responseCode = "404", description = "Équipe non trouvée", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping("/{teamId}")
    public Team getTeamById(@PathVariable Integer teamId) {
        return this.teamService.getTeamById(teamId);
    }

    @Operation(summary = "Créer une nouvelle équipe vide")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Équipe créée avec succès", content = @Content(schema = @Schema(implementation = Team.class))),
            @ApiResponse(responseCode = "400", description = "Données de création invalides", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Team createTeam(@RequestBody @Valid TeamRequest teamRequest) {
        return this.teamService.createTeam(teamRequest);
    }

    @Operation(summary = "Ajouter un athlète à une équipe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Athlète ajouté avec succès", content = @Content(schema = @Schema(implementation = Team.class))),
            @ApiResponse(responseCode = "404", description = "Équipe ou utilisateur non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PostMapping("/{teamId}/athletes/{athleteId}")
    public Team addAthlete(@PathVariable Integer teamId, @PathVariable Integer athleteId) {
        return this.teamService.addAthlete(teamId, athleteId);
    }

    @Operation(summary = "Retirer un athlète d'une équipe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Athlète retiré avec succès", content = @Content(schema = @Schema(implementation = Team.class))),
            @ApiResponse(responseCode = "404", description = "Équipe ou utilisateur non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @DeleteMapping("/{teamId}/athletes/{athleteId}")
    public Team removeAthlete(@PathVariable Integer teamId, @PathVariable Integer athleteId) {
        return this.teamService.removeAthlete(teamId, athleteId);
    }

    @Operation(summary = "Supprimer une équipe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Équipe supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Équipe non trouvée", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @DeleteMapping("/{teamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeam(@PathVariable Integer teamId) {
        this.teamService.deleteTeam(teamId);
    }
}