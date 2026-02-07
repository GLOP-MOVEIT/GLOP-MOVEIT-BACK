package com.moveit.championship.controller;

import com.moveit.championship.entity.Trial;
import com.moveit.championship.service.TrialService;
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
@RequestMapping("/trials")
@Tag(name = "Manches", description = "API de gestion des manches (trials)")
public class TrialController {

    private final TrialService trialService;

    @Operation(summary = "Récupérer une manche par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Manche récupérée avec succès", content = @Content(schema = @Schema(implementation = Trial.class))),
            @ApiResponse(responseCode = "404", description = "Manche non trouvée", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping("/{id}")
    public ResponseEntity<Trial> getTrialById(@PathVariable Integer id) {
        Trial trial = trialService.getTrialById(id);
        return ResponseEntity.ok(trial);
    }

    @Operation(summary = "Récupérer toutes les manches d'une compétition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Manches récupérées avec succès", content = @Content(schema = @Schema(implementation = Trial.class))),
            @ApiResponse(responseCode = "404", description = "Compétition non trouvée", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping("/competition/{competitionId}")
    public ResponseEntity<List<Trial>> getTrialsByCompetitionId(@PathVariable Integer competitionId) {
        List<Trial> trials = trialService.getTrialsByCompetitionId(competitionId);
        return ResponseEntity.ok(trials);
    }

    @Operation(summary = "Créer une nouvelle manche pour une compétition (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Manche créée avec succès", content = @Content(schema = @Schema(implementation = Trial.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Compétition non trouvée", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PostMapping("/competition/{competitionId}")
    public ResponseEntity<Trial> createTrial(@PathVariable Integer competitionId, @Valid @RequestBody Trial trial) {
        Trial createdTrial = trialService.createTrial(competitionId, trial);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTrial);
    }

    @Operation(summary = "Mettre à jour une manche (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Manche mise à jour avec succès", content = @Content(schema = @Schema(implementation = Trial.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Manche non trouvée", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PutMapping("/{id}")
    public ResponseEntity<Trial> updateTrial(@PathVariable Integer id, @Valid @RequestBody Trial trial) {
        Trial updatedTrial = trialService.updateTrial(id, trial);
        return ResponseEntity.ok(updatedTrial);
    }

    @Operation(summary = "Supprimer une manche (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Manche supprimée avec succès", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Manche non trouvée", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrial(@PathVariable Integer id) {
        trialService.deleteTrial(id);
        return ResponseEntity.noContent().build();
    }
}
