package com.moveit.user.controller;

import com.moveit.user.dto.Ticket;
import com.moveit.user.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
@Tag(name = "Ticket", description = "API de gestion des tickets")
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "Récupérer tous les tickets d'un utilisateur avec pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tickets récupérés avec succès", content = @Content(schema = @Schema(implementation = Ticket.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("/{userId}")
    public Page<Ticket> getTickets(@PathVariable Integer userId, Pageable pageable) {
        return this.ticketService.getTickets(pageable);
    }

    @Operation(summary = "Récupérer un ticket par ID pour un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket récupéré avec succès", content = @Content(schema = @Schema(implementation = Ticket.class))),
            @ApiResponse(responseCode = "404", description = "Ticket ou utilisateur non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("/{userId}/{ticketId}")
    public Ticket getTicket(@PathVariable Integer userId, @PathVariable Integer ticketId) {
        return this.ticketService.getTicketById(ticketId);
    }

    @Operation(summary = "Créer un nouveau ticket pour un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ticket créé avec succès", content = @Content(schema = @Schema(implementation = Ticket.class))),
            @ApiResponse(responseCode = "400", description = "Données de ticket invalides"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @PostMapping("/{userId}")
    public Ticket createTicket(@PathVariable Integer userId, @RequestBody Ticket ticket) {
        return this.ticketService.createTicket(userId, ticket);
    }
}