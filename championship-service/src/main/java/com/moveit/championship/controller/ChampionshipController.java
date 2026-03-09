package com.moveit.championship.controller;

import com.moveit.championship.dto.ChampionshipDTO;
import com.moveit.championship.dto.ChampionshipSummaryDTO;
import com.moveit.championship.dto.ChampionshipUpdateDTO;
import com.moveit.championship.entity.Championship;
import com.moveit.championship.mapper.ChampionshipMapper;
import com.moveit.championship.service.ChampionshipService;
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
@RequestMapping("/championships")
@Tag(name = "Championnats", description = "API de gestion des championnats")
public class ChampionshipController {

    private final ChampionshipService championshipService;

    @Operation(summary = "Récupérer tous les championnats")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Championnats récupérés avec succès", content = @Content(schema = @Schema(implementation = ChampionshipSummaryDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping
    public ResponseEntity<List<ChampionshipSummaryDTO>> getAllChampionships() {
        List<Championship> championships = championshipService.getAllChampionships();
        return ResponseEntity.ok(ChampionshipMapper.toChampionshipSummaryDTOList(championships));
    }

    @Operation(summary = "Récupérer un championnat par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Championnat récupéré avec succès", content = @Content(schema = @Schema(implementation = ChampionshipDTO.class))),
            @ApiResponse(responseCode = "404", description = "Championnat non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping("/{id}")
    public ResponseEntity<ChampionshipDTO> getChampionshipById(@PathVariable Integer id) {
        Championship championship = championshipService.getChampionshipById(id);
        return ResponseEntity.ok(ChampionshipMapper.toChampionshipDTO(championship));
    }

    @Operation(summary = "Créer un nouveau championnat (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Championnat créé avec succès", content = @Content(schema = @Schema(implementation = Championship.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PostMapping
    public ResponseEntity<ChampionshipDTO> createChampionship(@Valid @RequestBody Championship championship) {
        Championship createdChampionship = championshipService.createChampionship(championship);
        return ResponseEntity.status(HttpStatus.CREATED).body(ChampionshipMapper.toChampionshipDTO(createdChampionship));
    }

    @Operation(summary = "Mettre à jour un championnat (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Championnat mis à jour avec succès", content = @Content(schema = @Schema(implementation = Championship.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Championnat non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PutMapping("/{id}")
    public ResponseEntity<ChampionshipDTO> updateChampionship(@PathVariable Integer id, @Valid @RequestBody ChampionshipUpdateDTO dto) {
        Championship updatedChampionship = championshipService.updateChampionship(id, dto);
        return ResponseEntity.ok(ChampionshipMapper.toChampionshipDTO(updatedChampionship));
    }

    @Operation(summary = "Supprimer un championnat (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Championnat supprimé avec succès", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Championnat non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChampionship(@PathVariable Integer id) {
        championshipService.deleteChampionship(id);
        return ResponseEntity.noContent().build();
    }
}