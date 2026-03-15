package com.moveit.location.controller;

import org.springframework.web.bind.annotation.*;

import com.moveit.location.dto.LocationDTO;
import com.moveit.location.entity.Location;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/locations")
@Tag(name = "Lieux", description = "API de gestion des lieux")
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "Récupérer tous les lieux")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lieux récupérés avec succès", content = @Content(schema = @Schema(implementation = LocationDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        List<Location> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations.stream().map(this::toLocationDTO).toList());
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
        return ResponseEntity.ok(toLocationDTO(location));
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
        Location location = toLocationEntity(dto);
        Location createdLocation = locationService.createLocation(location);
        return ResponseEntity.status(HttpStatus.CREATED).body(toLocationDTO(createdLocation));
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
        Location location = toLocationEntity(dto);
        Location updatedLocation = locationService.updateLocation(id, location);
        return ResponseEntity.ok(toLocationDTO(updatedLocation));
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

    private LocationDTO toLocationDTO(Location location) {
        return LocationDTO.builder()
                .locationId(location.getLocationId())
                .name(location.getName())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .mainEntrance(location.getMainEntrance())
                .refereeEntrance(location.getRefereeEntrance())
                .athleteEntrance(location.getAthleteEntrance())
                .description(location.getDescription())
                .build();
    }

    private Location toLocationEntity(LocationDTO dto) {
        Location location = new Location();
        location.setLocationId(dto.getLocationId());
        location.setName(dto.getName());
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setMainEntrance(dto.getMainEntrance());
        location.setRefereeEntrance(dto.getRefereeEntrance());
        location.setAthleteEntrance(dto.getAthleteEntrance());
        location.setDescription(dto.getDescription());
        return location;
    }
}
