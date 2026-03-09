package com.moveit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.user.dto.Ticket;
import com.moveit.user.exception.GlobalExceptionHandler;
import com.moveit.user.exception.TicketNotFoundException;
import com.moveit.user.exception.UserNotFoundException;
import com.moveit.user.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(ticketController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();

        testTicket = new Ticket();
        testTicket.setId(1);
        testTicket.setTicketNumber("TKT-12345");
        testTicket.setSeatInformation("Section A, Row 5, Seat 10");
        testTicket.setEventDate("2026-03-15T18:00:00");
    }

    @Test
    void getTickets_ShouldReturnPageOfTickets() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Ticket> ticketPage = new PageImpl<>(List.of(testTicket), pageable, 1);

        when(ticketService.getTickets(any(Pageable.class))).thenReturn(ticketPage);

        mockMvc.perform(get("/tickets/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].ticketNumber").value("TKT-12345"))
                .andExpect(jsonPath("$.content[0].seatInformation").value("Section A, Row 5, Seat 10"))
                .andExpect(jsonPath("$.content[0].eventDate").value("2026-03-15T18:00:00"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(ticketService).getTickets(any(Pageable.class));
    }

    @Test
    void getTickets_ShouldReturnEmptyPage_WhenNoTickets() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Ticket> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(ticketService.getTickets(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/tickets/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(ticketService).getTickets(any(Pageable.class));
    }

    @Test
    void getTicket_ShouldReturnTicket_WhenTicketExists() throws Exception {
        when(ticketService.getTicketById(1)).thenReturn(testTicket);

        mockMvc.perform(get("/tickets/1/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ticketNumber").value("TKT-12345"))
                .andExpect(jsonPath("$.seatInformation").value("Section A, Row 5, Seat 10"))
                .andExpect(jsonPath("$.eventDate").value("2026-03-15T18:00:00"));

        verify(ticketService).getTicketById(1);
    }

    @Test
    void getTicket_ShouldReturnNotFound_WhenTicketDoesNotExist() throws Exception {
        when(ticketService.getTicketById(999)).thenThrow(new TicketNotFoundException("Ticket with id 999 not found"));

        mockMvc.perform(get("/tickets/999/"))
                .andExpect(status().isNotFound());

        verify(ticketService).getTicketById(999);
    }

    @Test
    void createTicket_ShouldCreateTicket_WhenValidRequest() throws Exception {
        Ticket newTicket = new Ticket();
        newTicket.setTicketNumber("TKT-67890");
        newTicket.setSeatInformation("Section B, Row 3, Seat 7");
        newTicket.setEventDate("2026-04-20T19:30:00");

        Ticket createdTicket = new Ticket();
        createdTicket.setId(2);
        createdTicket.setTicketNumber("TKT-67890");
        createdTicket.setSeatInformation("Section B, Row 3, Seat 7");
        createdTicket.setEventDate("2026-04-20T19:30:00");

        when(ticketService.createTicket(eq(1), any(Ticket.class))).thenReturn(createdTicket);

        mockMvc.perform(post("/tickets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTicket)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.ticketNumber").value("TKT-67890"))
                .andExpect(jsonPath("$.seatInformation").value("Section B, Row 3, Seat 7"))
                .andExpect(jsonPath("$.eventDate").value("2026-04-20T19:30:00"));

        verify(ticketService).createTicket(eq(1), any(Ticket.class));
    }

    @Test
    void createTicket_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        Ticket newTicket = new Ticket();
        newTicket.setTicketNumber("TKT-67890");
        newTicket.setSeatInformation("Section B, Row 3, Seat 7");
        newTicket.setEventDate("2026-04-20T19:30:00");

        when(ticketService.createTicket(eq(999), any(Ticket.class)))
                .thenThrow(new UserNotFoundException(999));

        mockMvc.perform(post("/tickets/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTicket)))
                .andExpect(status().isNotFound());

        verify(ticketService).createTicket(eq(999), any(Ticket.class));
    }
}



