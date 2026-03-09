package com.moveit.user.service;

import com.moveit.user.dto.Ticket;
import com.moveit.user.entity.TicketEntity;
import com.moveit.user.entity.UserEntity;
import com.moveit.user.exception.TicketNotFoundException;
import com.moveit.user.mapper.TicketMapper;
import com.moveit.user.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

        TicketEntity savedTicket = this.ticketRepository.save(ticketEntity);
        return this.ticketMapper.toDto(savedTicket);
    }
}