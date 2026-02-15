package com.moveit.user.service;

import com.moveit.user.dto.Ticket;
import com.moveit.user.entity.TicketEntity;
import com.moveit.user.entity.UserEntity;
import com.moveit.user.exception.TicketNotFoundException;
import com.moveit.user.exception.UserNotFoundException;
import com.moveit.user.mapper.TicketMapper;
import com.moveit.user.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserService userService;

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketService ticketService;

    private TicketEntity testTicketEntity;
    private Ticket testTicket;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setUserId(1);
        testUser.setFirstName("John");
        testUser.setSurname("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPhoneNumber("+33123456789");
        testUser.setLanguage("FR");
        testUser.setAcceptsNotifications(true);

        testTicketEntity = new TicketEntity();
        testTicketEntity.setId(1);
        testTicketEntity.setTicketNumber("TKT-12345");
        testTicketEntity.setSeatInformation("Section A, Row 5, Seat 10");
        testTicketEntity.setEventDate(Instant.parse("2026-03-15T18:00:00Z"));
        testTicketEntity.setUser(testUser);

        testTicket = new Ticket();
        testTicket.setId(1);
        testTicket.setTicketNumber("TKT-12345");
        testTicket.setSeatInformation("Section A, Row 5, Seat 10");
        testTicket.setEventDate("2026-03-15T18:00:00");
    }

    @Test
    void getTickets_ShouldReturnPageOfTickets() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TicketEntity> ticketEntityPage = new PageImpl<>(List.of(testTicketEntity), pageable, 1);

        when(ticketRepository.findAll(pageable)).thenReturn(ticketEntityPage);
        when(ticketMapper.toDto(testTicketEntity)).thenReturn(testTicket);

        Page<Ticket> result = ticketService.getTickets(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getTicketNumber()).isEqualTo("TKT-12345");

        verify(ticketRepository).findAll(pageable);
        verify(ticketMapper).toDto(testTicketEntity);
    }

    @Test
    void getTickets_ShouldReturnEmptyPage_WhenNoTickets() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TicketEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(ticketRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<Ticket> result = ticketService.getTickets(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(ticketRepository).findAll(pageable);
        verify(ticketMapper, never()).toDto(any());
    }

    @Test
    void getTicketById_ShouldReturnTicket_WhenTicketExists() {
        when(ticketRepository.findById(1)).thenReturn(Optional.of(testTicketEntity));
        when(ticketMapper.toDto(testTicketEntity)).thenReturn(testTicket);

        Ticket result = ticketService.getTicketById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getTicketNumber()).isEqualTo("TKT-12345");
        assertThat(result.getSeatInformation()).isEqualTo("Section A, Row 5, Seat 10");

        verify(ticketRepository).findById(1);
        verify(ticketMapper).toDto(testTicketEntity);
    }

    @Test
    void getTicketById_ShouldThrowTicketNotFoundException_WhenTicketNotFound() {
        when(ticketRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicketById(999))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessage("Ticket with id 999 not found");

        verify(ticketRepository).findById(999);
        verify(ticketMapper, never()).toDto(any());
    }

    @Test
    void createTicket_ShouldCreateTicketAndAssociateWithUser() {
        Ticket newTicket = new Ticket();
        newTicket.setTicketNumber("TKT-67890");
        newTicket.setSeatInformation("Section B, Row 3, Seat 7");
        newTicket.setEventDate("2026-04-20T19:30:00");

        TicketEntity newTicketEntity = new TicketEntity();
        newTicketEntity.setTicketNumber("TKT-67890");
        newTicketEntity.setSeatInformation("Section B, Row 3, Seat 7");
        newTicketEntity.setEventDate(Instant.parse("2026-04-20T19:30:00Z"));

        TicketEntity savedTicketEntity = new TicketEntity();
        savedTicketEntity.setId(2);
        savedTicketEntity.setTicketNumber("TKT-67890");
        savedTicketEntity.setSeatInformation("Section B, Row 3, Seat 7");
        savedTicketEntity.setEventDate(Instant.parse("2026-04-20T19:30:00Z"));
        savedTicketEntity.setUser(testUser);

        Ticket savedTicket = new Ticket();
        savedTicket.setId(2);
        savedTicket.setTicketNumber("TKT-67890");
        savedTicket.setSeatInformation("Section B, Row 3, Seat 7");
        savedTicket.setEventDate("2026-04-20T19:30:00");

        when(userService.getUserEntityById(1)).thenReturn(testUser);
        when(ticketMapper.toEntity(newTicket)).thenReturn(newTicketEntity);
        when(ticketRepository.save(any(TicketEntity.class))).thenReturn(savedTicketEntity);
        when(ticketMapper.toDto(savedTicketEntity)).thenReturn(savedTicket);

        Ticket result = ticketService.createTicket(1, newTicket);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2);
        assertThat(result.getTicketNumber()).isEqualTo("TKT-67890");

        ArgumentCaptor<TicketEntity> captor = ArgumentCaptor.forClass(TicketEntity.class);
        verify(ticketRepository).save(captor.capture());

        TicketEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getUser()).isEqualTo(testUser);

        verify(userService).getUserEntityById(1);
        verify(ticketMapper).toEntity(newTicket);
        verify(ticketMapper).toDto(savedTicketEntity);
    }

    @Test
    void createTicket_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        Ticket newTicket = new Ticket();
        newTicket.setTicketNumber("TKT-67890");
        newTicket.setSeatInformation("Section B, Row 3, Seat 7");
        newTicket.setEventDate("2026-04-20T19:30:00");

        when(userService.getUserEntityById(999)).thenThrow(new UserNotFoundException(999));

        assertThatThrownBy(() -> ticketService.createTicket(999, newTicket))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userService).getUserEntityById(999);
        verify(ticketMapper, never()).toEntity(any());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void createTicket_ShouldHandleMultipleTicketsForSameUser() {
        Ticket ticket1 = new Ticket();
        ticket1.setTicketNumber("TKT-00001");
        ticket1.setSeatInformation("Section A, Row 1, Seat 1");
        ticket1.setEventDate("2026-05-01T20:00:00");

        Ticket ticket2 = new Ticket();
        ticket2.setTicketNumber("TKT-00002");
        ticket2.setSeatInformation("Section A, Row 1, Seat 2");
        ticket2.setEventDate("2026-05-01T20:00:00");

        TicketEntity ticketEntity1 = new TicketEntity();
        ticketEntity1.setTicketNumber("TKT-00001");
        ticketEntity1.setSeatInformation("Section A, Row 1, Seat 1");

        TicketEntity ticketEntity2 = new TicketEntity();
        ticketEntity2.setTicketNumber("TKT-00002");
        ticketEntity2.setSeatInformation("Section A, Row 1, Seat 2");

        TicketEntity savedEntity1 = new TicketEntity();
        savedEntity1.setId(3);
        savedEntity1.setTicketNumber("TKT-00001");
        savedEntity1.setUser(testUser);

        TicketEntity savedEntity2 = new TicketEntity();
        savedEntity2.setId(4);
        savedEntity2.setTicketNumber("TKT-00002");
        savedEntity2.setUser(testUser);

        Ticket saved1 = new Ticket();
        saved1.setId(3);
        saved1.setTicketNumber("TKT-00001");

        Ticket saved2 = new Ticket();
        saved2.setId(4);
        saved2.setTicketNumber("TKT-00002");

        when(userService.getUserEntityById(1)).thenReturn(testUser);
        when(ticketMapper.toEntity(ticket1)).thenReturn(ticketEntity1);
        when(ticketMapper.toEntity(ticket2)).thenReturn(ticketEntity2);
        when(ticketRepository.save(ticketEntity1)).thenReturn(savedEntity1);
        when(ticketRepository.save(ticketEntity2)).thenReturn(savedEntity2);
        when(ticketMapper.toDto(savedEntity1)).thenReturn(saved1);
        when(ticketMapper.toDto(savedEntity2)).thenReturn(saved2);

        Ticket result1 = ticketService.createTicket(1, ticket1);
        Ticket result2 = ticketService.createTicket(1, ticket2);

        assertThat(result1.getId()).isEqualTo(3);
        assertThat(result2.getId()).isEqualTo(4);

        verify(userService, times(2)).getUserEntityById(1);
        verify(ticketRepository, times(2)).save(any(TicketEntity.class));
    }
}

