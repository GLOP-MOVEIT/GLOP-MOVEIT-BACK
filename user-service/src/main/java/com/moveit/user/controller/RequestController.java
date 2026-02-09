package com.moveit.user.controller;

import com.moveit.user.dto.RefuseRequest;
import com.moveit.user.dto.Request;
import com.moveit.user.service.RequestService;
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
@RequestMapping("/requests")
@Tag(name = "Request", description = "API de gestion des demandes de promotion")
public class RequestController {

    private final RequestService requestService;

    @Operation(summary = "Récupérer toutes les demandes de promotion avec pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demandes de promotion récupérées avec succès", content = @Content(schema = @Schema(implementation = Request.class))),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping
    public Page<Request> getAllRequests(Pageable pageable) {
        return this.requestService.getAllRequests(pageable);
    }

    @Operation(summary = "Récupérer une demande de promotion par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demande de promotion récupérée avec succès", content = @Content(schema = @Schema(implementation = Request.class))),
            @ApiResponse(responseCode = "404", description = "Demande de promotion non trouvée", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping("/{id}")
    public Request getRequestById(@PathVariable Integer id) {
        return this.requestService.getRequestById(id);
    }

    @Operation(summary = "Créer une demande de promotion pour un athlète")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demande de promotion créée avec succès", content = @Content(schema = @Schema(implementation = Request.class))),
            @ApiResponse(responseCode = "404", description = "Athlète non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PostMapping("/athlete/{id}")
    public Request requestToAthlete(@PathVariable Integer id) {
        return this.requestService.createAthleteRequest(id);
    }

    @Operation(summary = "Créer une demande de promotion pour un bénévole")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demande de promotion créée avec succès", content = @Content(schema = @Schema(implementation = Request.class))),
            @ApiResponse(responseCode = "404", description = "Bénévole non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PostMapping("/volunteer/{id}")
    public Request requestToVolunteer(@PathVariable Integer id) {
        return this.requestService.createVolunteerRequest(id);
    }

    @GetMapping("/accept/{id}")
    public void acceptRequest(@PathVariable Integer id) {
        this.requestService.acceptRequest(id);
    }

    @GetMapping("/refuse/{id}")
    public void refuseRequest(@PathVariable Integer id, @RequestBody @Valid RefuseRequest refuseRequest) {
        this.requestService.refuseRequest(id, refuseRequest);
    }
}