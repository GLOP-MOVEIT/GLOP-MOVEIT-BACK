package com.moveit.user.service;

import com.moveit.user.dto.RejectRequest;
import com.moveit.user.dto.Request;
import com.moveit.user.dto.RequestStatus;
import com.moveit.user.entity.RequestEntity;
import com.moveit.user.entity.RoleEntity;
import com.moveit.user.entity.UserEntity;
import com.moveit.user.exception.RequestNotFoundException;
import com.moveit.user.mapper.RequestMapper;
import com.moveit.user.repository.RequestRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private RequestMapper requestMapper;

    @Mock
    private UserService userService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RequestService requestService;

    private RequestEntity testRequestEntity;
    private Request testRequest;
    private UserEntity testUser;
    private RoleEntity athleteRole;
    private RoleEntity volunteerRole;

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

        athleteRole = new RoleEntity();
        athleteRole.setRoleId(1);
        athleteRole.setName("ATHLETE");

        volunteerRole = new RoleEntity();
        volunteerRole.setRoleId(2);
        volunteerRole.setName("VOLUNTEER");

        testRequestEntity = new RequestEntity();
        testRequestEntity.setRequestId(1);
        testRequestEntity.setRequestStatus(RequestStatus.PENDING);
        testRequestEntity.setUser(testUser);
        testRequestEntity.setRole(athleteRole);
        testRequestEntity.setDocuments(List.of());

        testRequest = new Request();
        testRequest.setRequestId(1);
        testRequest.setRequestStatus(RequestStatus.PENDING);
    }

    @Test
    void getAllRequests_ShouldReturnPageOfRequests() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RequestEntity> requestEntityPage = new PageImpl<>(List.of(testRequestEntity), pageable, 1);

        when(requestRepository.findAll(pageable)).thenReturn(requestEntityPage);
        when(requestMapper.toDto(testRequestEntity)).thenReturn(testRequest);

        Page<Request> result = requestService.getAllRequests(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getRequestId()).isEqualTo(1);

        verify(requestRepository).findAll(pageable);
        verify(requestMapper).toDto(testRequestEntity);
    }

    @Test
    void getAllRequests_ShouldReturnEmptyPage_WhenNoRequests() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RequestEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(requestRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<Request> result = requestService.getAllRequests(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(requestRepository).findAll(pageable);
        verify(requestMapper, never()).toDto(any());
    }

    @Test
    void getRequestById_ShouldReturnRequest_WhenRequestExists() {
        when(requestRepository.findById(1)).thenReturn(Optional.of(testRequestEntity));
        when(requestMapper.toDto(testRequestEntity)).thenReturn(testRequest);

        Request result = requestService.getRequestById(1);

        assertThat(result).isNotNull();
        assertThat(result.getRequestId()).isEqualTo(1);
        assertThat(result.getRequestStatus()).isEqualTo(RequestStatus.PENDING);

        verify(requestRepository).findById(1);
        verify(requestMapper).toDto(testRequestEntity);
    }

    @Test
    void getRequestById_ShouldThrowRequestNotFoundException_WhenRequestNotFound() {
        when(requestRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getRequestById(999))
                .isInstanceOf(RequestNotFoundException.class)
                .hasMessage("Request with id 999 not found");

        verify(requestRepository).findById(999);
        verify(requestMapper, never()).toDto(any());
    }

    @Test
    void createAthleteRequest_ShouldCreateRequestWithAthleteRole() {
        when(userService.getUserEntityById(1)).thenReturn(testUser);
        when(roleService.getRoleEntityByName("ATHLETE")).thenReturn(athleteRole);
        when(requestRepository.save(any(RequestEntity.class))).thenReturn(testRequestEntity);
        when(requestMapper.toDto(testRequestEntity)).thenReturn(testRequest);

        Request result = requestService.createAthleteRequest(1);

        assertThat(result).isNotNull();
        assertThat(result.getRequestId()).isEqualTo(1);

        ArgumentCaptor<RequestEntity> captor = ArgumentCaptor.forClass(RequestEntity.class);
        verify(requestRepository).save(captor.capture());

        RequestEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getRequestStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(savedEntity.getUser()).isEqualTo(testUser);
        assertThat(savedEntity.getRole()).isEqualTo(athleteRole);

        verify(userService).getUserEntityById(1);
        verify(roleService).getRoleEntityByName("ATHLETE");
        verify(requestMapper).toDto(testRequestEntity);
    }

    @Test
    void createVolunteerRequest_ShouldCreateRequestWithVolunteerRole() {
        RequestEntity volunteerRequestEntity = new RequestEntity();
        volunteerRequestEntity.setRequestId(2);
        volunteerRequestEntity.setRequestStatus(RequestStatus.PENDING);
        volunteerRequestEntity.setUser(testUser);
        volunteerRequestEntity.setRole(volunteerRole);

        Request volunteerRequest = new Request();
        volunteerRequest.setRequestId(2);
        volunteerRequest.setRequestStatus(RequestStatus.PENDING);

        when(userService.getUserEntityById(1)).thenReturn(testUser);
        when(roleService.getRoleEntityByName("VOLUNTEER")).thenReturn(volunteerRole);
        when(requestRepository.save(any(RequestEntity.class))).thenReturn(volunteerRequestEntity);
        when(requestMapper.toDto(volunteerRequestEntity)).thenReturn(volunteerRequest);

        Request result = requestService.createVolunteerRequest(1);

        assertThat(result).isNotNull();
        assertThat(result.getRequestId()).isEqualTo(2);

        ArgumentCaptor<RequestEntity> captor = ArgumentCaptor.forClass(RequestEntity.class);
        verify(requestRepository).save(captor.capture());

        RequestEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getRequestStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(savedEntity.getUser()).isEqualTo(testUser);
        assertThat(savedEntity.getRole()).isEqualTo(volunteerRole);

        verify(userService).getUserEntityById(1);
        verify(roleService).getRoleEntityByName("VOLUNTEER");
        verify(requestMapper).toDto(volunteerRequestEntity);
    }

    @Test
    void acceptRequest_ShouldApproveRequestAndUpdateUserRole() {
        when(requestRepository.findById(1)).thenReturn(Optional.of(testRequestEntity));
        when(requestRepository.save(any(RequestEntity.class))).thenReturn(testRequestEntity);

        requestService.acceptRequest(1);

        ArgumentCaptor<RequestEntity> requestCaptor = ArgumentCaptor.forClass(RequestEntity.class);
        verify(requestRepository).save(requestCaptor.capture());

        RequestEntity savedRequest = requestCaptor.getValue();
        assertThat(savedRequest.getRequestStatus()).isEqualTo(RequestStatus.APPROVED);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userService).saveUserEntity(userCaptor.capture());

        UserEntity savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo(athleteRole);

        verify(requestRepository).findById(1);
    }

    @Test
    void acceptRequest_ShouldThrowRequestNotFoundException_WhenRequestNotFound() {
        when(requestRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.acceptRequest(999))
                .isInstanceOf(RequestNotFoundException.class)
                .hasMessage("Request with id 999 not found");

        verify(requestRepository).findById(999);
        verify(requestRepository, never()).save(any());
        verify(userService, never()).saveUserEntity(any());
    }

    @Test
    void rejectRequest_ShouldRejectRequestWithReason() {
        RejectRequest rejectRequest = new RejectRequest("Invalid documentation");

        when(requestRepository.findById(1)).thenReturn(Optional.of(testRequestEntity));
        when(requestRepository.save(any(RequestEntity.class))).thenReturn(testRequestEntity);

        requestService.rejectRequest(1, rejectRequest);

        ArgumentCaptor<RequestEntity> captor = ArgumentCaptor.forClass(RequestEntity.class);
        verify(requestRepository).save(captor.capture());

        RequestEntity savedRequest = captor.getValue();
        assertThat(savedRequest.getRequestStatus()).isEqualTo(RequestStatus.REJECTED);
        assertThat(savedRequest.getRequestRejectionReason()).isEqualTo("Invalid documentation");

        verify(requestRepository).findById(1);
    }

    @Test
    void rejectRequest_ShouldThrowRequestNotFoundException_WhenRequestNotFound() {
        RejectRequest rejectRequest = new RejectRequest("Invalid documentation");

        when(requestRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.rejectRequest(999, rejectRequest))
                .isInstanceOf(RequestNotFoundException.class)
                .hasMessage("Request with id 999 not found");

        verify(requestRepository).findById(999);
        verify(requestRepository, never()).save(any());
    }
}

