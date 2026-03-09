package com.moveit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.user.dto.RejectRequest;
import com.moveit.user.dto.Request;
import com.moveit.user.dto.RequestStatus;
import com.moveit.user.dto.Role;
import com.moveit.user.exception.GlobalExceptionHandler;
import com.moveit.user.exception.RequestNotFoundException;
import com.moveit.user.service.RequestService;
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
class RequestControllerTest {

    @Mock
    private RequestService requestService;

    @InjectMocks
    private RequestController requestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Request testRequest;
    private RejectRequest rejectRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(requestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();

        Role role = new Role("ATHLETE");
        testRequest = new Request();
        testRequest.setRequestId(1);
        testRequest.setRequestStatus(RequestStatus.PENDING);
        testRequest.setRole(role);
        testRequest.setDocuments(List.of());

        rejectRequest = new RejectRequest("Invalid documentation");
    }

    @Test
    void getAllRequests_ShouldReturnPageOfRequests() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Request> requestPage = new PageImpl<>(List.of(testRequest), pageable, 1);

        when(requestService.getAllRequests(any(Pageable.class))).thenReturn(requestPage);

        mockMvc.perform(get("/requests")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].requestId").value(1))
                .andExpect(jsonPath("$.content[0].requestStatus").value("PENDING"))
                .andExpect(jsonPath("$.content[0].role.name").value("ATHLETE"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(requestService).getAllRequests(any(Pageable.class));
    }

    @Test
    void getAllRequests_ShouldReturnEmptyPage_WhenNoRequests() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Request> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(requestService.getAllRequests(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/requests")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(requestService).getAllRequests(any(Pageable.class));
    }

    @Test
    void getRequestById_ShouldReturnRequest_WhenRequestExists() throws Exception {
        when(requestService.getRequestById(1)).thenReturn(testRequest);

        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(1))
                .andExpect(jsonPath("$.requestStatus").value("PENDING"))
                .andExpect(jsonPath("$.role.name").value("ATHLETE"));

        verify(requestService).getRequestById(1);
    }

    @Test
    void getRequestById_ShouldReturnNotFound_WhenRequestDoesNotExist() throws Exception {
        when(requestService.getRequestById(999)).thenThrow(new RequestNotFoundException("Request with id 999 not found"));

        mockMvc.perform(get("/requests/999"))
                .andExpect(status().isNotFound());

        verify(requestService).getRequestById(999);
    }

    @Test
    void requestToAthlete_ShouldCreateAthleteRequest() throws Exception {
        when(requestService.createAthleteRequest(1)).thenReturn(testRequest);

        mockMvc.perform(post("/requests/athlete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(1))
                .andExpect(jsonPath("$.requestStatus").value("PENDING"))
                .andExpect(jsonPath("$.role.name").value("ATHLETE"));

        verify(requestService).createAthleteRequest(1);
    }

    @Test
    void requestToVolunteer_ShouldCreateVolunteerRequest() throws Exception {
        Role volunteerRole = new Role("VOLUNTEER");
        Request volunteerRequest = new Request();
        volunteerRequest.setRequestId(2);
        volunteerRequest.setRequestStatus(RequestStatus.PENDING);
        volunteerRequest.setRole(volunteerRole);
        volunteerRequest.setDocuments(List.of());

        when(requestService.createVolunteerRequest(1)).thenReturn(volunteerRequest);

        mockMvc.perform(post("/requests/volunteer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(2))
                .andExpect(jsonPath("$.requestStatus").value("PENDING"))
                .andExpect(jsonPath("$.role.name").value("VOLUNTEER"));

        verify(requestService).createVolunteerRequest(1);
    }

    @Test
    void acceptRequest_ShouldAcceptRequest_WhenRequestExists() throws Exception {
        doNothing().when(requestService).acceptRequest(1);

        mockMvc.perform(get("/requests/accept/1"))
                .andExpect(status().isOk());

        verify(requestService).acceptRequest(1);
    }

    @Test
    void acceptRequest_ShouldReturnNotFound_WhenRequestDoesNotExist() throws Exception {
        doThrow(new RequestNotFoundException("Request with id 999 not found"))
                .when(requestService).acceptRequest(999);

        mockMvc.perform(get("/requests/accept/999"))
                .andExpect(status().isNotFound());

        verify(requestService).acceptRequest(999);
    }

    @Test
    void rejectRequest_ShouldRejectRequest_WhenRequestExists() throws Exception {
        doNothing().when(requestService).rejectRequest(eq(1), any(RejectRequest.class));

        mockMvc.perform(get("/requests/reject/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isOk());

        verify(requestService).rejectRequest(eq(1), any(RejectRequest.class));
    }

    @Test
    void rejectRequest_ShouldReturnNotFound_WhenRequestDoesNotExist() throws Exception {
        doThrow(new RequestNotFoundException("Request with id 999 not found"))
                .when(requestService).rejectRequest(eq(999), any(RejectRequest.class));

        mockMvc.perform(get("/requests/reject/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isNotFound());

        verify(requestService).rejectRequest(eq(999), any(RejectRequest.class));
    }
}


