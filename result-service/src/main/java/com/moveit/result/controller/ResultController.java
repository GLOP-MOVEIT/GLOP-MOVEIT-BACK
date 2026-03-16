package com.moveit.result.controller;

import com.moveit.result.dto.Result;
import com.moveit.result.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/results")
@Tag(name = "Result", description = "API de gestion des résultats")
public class ResultController {

    private final ResultService resultService;

    @Operation(summary = "Récupérer le résultat d'une épreuve par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résultat récupéré avec succès", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "404", description = "Résultat non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping("/trial/{trialId}")
    public Result getResultByTrialId(@PathVariable Integer trialId) {
        return this.resultService.getResultByTrialId(trialId);
    }

    @Operation(summary = "Enregistrer un résultat d'épreuve")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résultat enregistré avec succès", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PostMapping
    public Result saveResult(@RequestBody @Valid Result result) {
        return this.resultService.saveResult(result);
    }

    @Operation(summary = "Mettre à jour un résultat d'épreuve")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résultat mis à jour avec succès", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Résultat non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PutMapping
    public Result updateResult(@RequestBody @Valid Result result) {
        return this.resultService.updateResult(result);
    }

    @Operation(summary = "Récupérer tous les résultats d'un participant par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résultats récupérés avec succès", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "404", description = "Résultats non trouvés", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping("/participant/{participantId}")
    public List<Result> getAllResultsByParticipantId(@PathVariable Integer participantId) {
        return this.resultService.getAllResultsByParticipantId(participantId);
    }
}