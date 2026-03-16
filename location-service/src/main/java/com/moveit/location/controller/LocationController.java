package com.moveit.location.controller;

import com.moveit.location.dto.*;
import com.moveit.location.entity.Location;
import com.moveit.location.mapper.LocationMapper;
import com.moveit.location.service.LocationLocatorService;
import com.moveit.location.service.LocationService;
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
@RequestMapping("/locations")
@Tag(name = "Lieux", description = "API de gestion des lieux")
public class LocationController {
    private final LocationService locationService;
    private final LocationMapper locationMapper;
    private final LocationLocatorService locatorService;

    @Operation(summary = "Récupérer tous les lieux")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lieux récupérés avec succès", content = @Content(schema = @Schema(implementation = LocationDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        List<Location> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations.stream().map(locationMapper::toDto).toList());
    }

    @Operation(summary = "Récupérer un lieu par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lieu récupéré avec succès", content = @Content(schema = @Schema(implementation = LocationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Lieu non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getLocationById(@PathVariable Integer id) {
        Location location = locationService.getLocationById(id);
        return ResponseEntity.ok(locationMapper.toDto(location));
    }

    @Operation(summary = "Créer un nouveau lieu (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lieu créé avec succès", content = @Content(schema = @Schema(implementation = LocationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PostMapping
    public ResponseEntity<LocationDTO> createLocation(@Valid @RequestBody LocationDTO dto) {
        Location location = locationMapper.toEntity(dto);
        Location createdLocation = locationService.createLocation(location);
        return ResponseEntity.status(HttpStatus.CREATED).body(locationMapper.toDto(createdLocation));
    }

    @Operation(summary = "Mettre à jour un lieu (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lieu mis à jour avec succès", content = @Content(schema = @Schema(implementation = LocationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Lieu non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PutMapping("/{id}")
    public ResponseEntity<LocationDTO> updateLocation(@PathVariable Integer id, @Valid @RequestBody LocationDTO dto) {
        Location location = locationMapper.toEntity(dto);
        Location updatedLocation = locationService.updateLocation(id, location);
        return ResponseEntity.ok(locationMapper.toDto(updatedLocation));
    }

    @Operation(summary = "Supprimer un lieu (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Lieu supprimé avec succès", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Lieu non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Integer id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Localiser un utilisateur (spectateur ou athlète)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Position renvoyée", content = @Content(schema = @Schema(implementation = LocateResponse.class))),
            @ApiResponse(responseCode = "403", description = "Non autorisé", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content())
    })
    @PostMapping("/locate")
    public ResponseEntity<LocateResponse> locateUser(@RequestBody LocateRequest request,
                                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        LocateResponse response = locatorService.locate(request, authorization);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Localiser tous les athlètes et volontaires d'une épreuve (Referee)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Positions renvoyées", content = @Content(schema = @Schema(implementation = BulkLocateTrialResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Non autorisé", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Epreuve ou utilisateur non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content())
    })
    @PostMapping("/locate/trial")
    public ResponseEntity<BulkLocateTrialResponse> locateAllForTrial(@Valid @RequestBody BulkLocateTrialRequest request,
                                                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        BulkLocateTrialResponse response = locatorService.locateAllForTrial(request, authorization);
        return ResponseEntity.ok(response);
    }
}
