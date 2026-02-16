package com.moveit.championship.controller;

import com.moveit.championship.dto.CompetitionDTO;
import com.moveit.championship.entity.Competition;
import com.moveit.championship.mapper.CompetitionMapper;
import com.moveit.championship.service.CompetitionService;
import com.moveit.championship.service.TreeGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/championships/competitions")
@Tag(name = "Compétitions", description = "API de gestion des compétitions")
public class CompetitionController {

    private final CompetitionService competitionService;
    private final TreeGenerationService treeGenerationService;

    @Operation(summary = "Récupérer toutes les compétitions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compétitions récupérées avec succès", content = @Content(schema = @Schema(implementation = CompetitionDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping
    public ResponseEntity<List<CompetitionDTO>> getAllCompetitions() {
        List<Competition> competitions = competitionService.getAllCompetitions();
        return ResponseEntity.ok(CompetitionMapper.toCompetitionDTOList(competitions));
    }

    @Operation(summary = "Récupérer une compétition par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compétition récupérée avec succès", content = @Content(schema = @Schema(implementation = CompetitionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Compétition non trouvée", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping("/{id}")
    public ResponseEntity<CompetitionDTO> getCompetitionById(@PathVariable Integer id) {
        Competition competition = competitionService.getCompetitionById(id);
        return ResponseEntity.ok(CompetitionMapper.toCompetitionDTO(competition));
    }

    @Operation(summary = "Créer une nouvelle compétition (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compétition créée avec succès", content = @Content(schema = @Schema(implementation = CompetitionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PostMapping
    public ResponseEntity<CompetitionDTO> createCompetition(@Valid @RequestBody Competition competition) {
        Competition createdCompetition = competitionService.createCompetition(competition);
        return ResponseEntity.status(HttpStatus.CREATED).body(CompetitionMapper.toCompetitionDTO(createdCompetition));
    }

    @Operation(summary = "Mettre à jour une compétition (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compétition mise à jour avec succès", content = @Content(schema = @Schema(implementation = CompetitionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Compétition non trouvée", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PutMapping("/{id}")
    public ResponseEntity<CompetitionDTO> updateCompetition(@PathVariable Integer id, @Valid @RequestBody Competition competition) {
        Competition updatedCompetition = competitionService.updateCompetition(id, competition);
        return ResponseEntity.ok(CompetitionMapper.toCompetitionDTO(updatedCompetition));
    }

    @Operation(summary = "Générer l'arbre complet d'une compétition (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arbre généré avec succès", content = @Content(schema = @Schema(implementation = CompetitionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Compétition non trouvée", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PostMapping("/{id}/generate-tree")
    public ResponseEntity<CompetitionDTO> generateTree(@PathVariable Integer id, @RequestBody List<Integer> participantIds) {
        Competition competition = treeGenerationService.generateTree(id, participantIds);
        return ResponseEntity.ok(CompetitionMapper.toCompetitionDTO(competition));
    }

    @Operation(summary = "Supprimer une compétition (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Compétition supprimée avec succès", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Compétition non trouvée", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompetition(@PathVariable Integer id) {
        competitionService.deleteCompetition(id);
        return ResponseEntity.noContent().build();
    }
}
