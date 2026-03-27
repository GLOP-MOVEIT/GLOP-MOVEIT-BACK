package com.moveit.user.service;

import com.moveit.user.dto.Ticket;
import com.moveit.user.dto.TicketVerificationResponse;
import com.moveit.user.entity.TicketEntity;
import com.moveit.user.entity.UserEntity;
import com.moveit.user.exception.TicketNotFoundException;
import com.moveit.user.mapper.TicketMapper;
import com.moveit.user.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final TicketMapper ticketMapper;

    public Page<Ticket> getTickets(Pageable pageable) {
        return this.ticketRepository.findAll(pageable)
                .map(this.ticketMapper::toDto);
    }

    public Ticket getTicketById(Integer ticketId) {
        return this.ticketRepository.findById(ticketId)
                .map(this.ticketMapper::toDto)
                .orElseThrow(() -> new TicketNotFoundException("Ticket with id " + ticketId + " not found"));
    }

    public Ticket createTicket(Integer userId, Ticket ticket) {
        UserEntity user = this.userService.getUserEntityById(userId);

        TicketEntity ticketEntity = this.ticketMapper.toEntity(ticket);
        ticketEntity.setUser(user);
        ticketEntity.setValidationToken(generateValidationToken());

        TicketEntity savedTicket = this.ticketRepository.save(ticketEntity);
        return this.ticketMapper.toDto(savedTicket);
    }

    public TicketVerificationResponse verifyTicket(String validationToken) {
        TicketEntity ticket = this.ticketRepository.findByValidationToken(validationToken)
                .orElseThrow(() -> new TicketNotFoundException("Ticket with validation token " + validationToken + " not found"));

        return new TicketVerificationResponse(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getSeatInformation(),
                ticket.getEventDate(),
                true
        );
    }

    private String generateValidationToken() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "");
        } while (this.ticketRepository.existsByValidationToken(token));

        return token;
    }
}
