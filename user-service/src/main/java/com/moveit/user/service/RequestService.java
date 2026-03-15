package com.moveit.user.service;

import com.moveit.user.dto.CoverLetter;
import com.moveit.user.dto.RejectRequest;
import com.moveit.user.dto.Request;
import com.moveit.user.dto.RequestStatus;
import com.moveit.user.entity.RequestEntity;
import com.moveit.user.entity.UserEntity;
import com.moveit.user.exception.RequestNotFoundException;
import com.moveit.user.exception.UserNotASpectatorException;
import com.moveit.user.mapper.RequestMapper;
import com.moveit.user.repository.RequestRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserService userService;
    private final RoleService roleService;

    public Page<Request> getAllRequests(Pageable pageable) {
        return this.requestRepository.findAll(pageable)
                .map(requestMapper::toDto);
    }

    public Request getRequestById(Integer id) {
        return this.requestRepository.findById(id)
                .map(requestMapper::toDto)
                .orElseThrow(() -> new RequestNotFoundException("Request with id " + id + " not found"));
    }

    public Request createAthleteRequest(Integer userId) {
        var user = this.verifyUserIsASpectator(userId);
        RequestEntity entity = new RequestEntity();
        entity.setRequestStatus(RequestStatus.PENDING);
        entity.setUser(user);
        entity.setRole(this.roleService.getRoleEntityByName("ATHLETE"));
        return this.requestMapper.toDto(this.requestRepository.save(entity));
    }

    public Request createVolunteerRequest(Integer userId, CoverLetter coverLetter) {
        var user = this.verifyUserIsASpectator(userId);
        RequestEntity entity = new RequestEntity();
        entity.setRequestStatus(RequestStatus.PENDING);
        entity.setUser(user);
        entity.setRole(this.roleService.getRoleEntityByName("VOLUNTEER"));
        entity.setCoverLetter(coverLetter.getCoverLetter());
        return this.requestMapper.toDto(this.requestRepository.save(entity));
    }

    public void acceptRequest(Integer id) {
        RequestEntity request = this.requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Request with id " + id + " not found"));

        request.setRequestStatus(RequestStatus.APPROVED);

        UserEntity user = request.getUser();
        user.setRole(request.getRole());

        this.userService.saveUserEntity(user);
        this.requestRepository.save(request);
    }

    public void rejectRequest(Integer id, RejectRequest refuseRequest) {
        RequestEntity request = this.requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Request with id " + id + " not found"));

        request.setRequestStatus(RequestStatus.REJECTED);
        request.setRequestRejectionReason(refuseRequest.getRequestRejectionReason());

        this.requestRepository.save(request);
    }

    public void promoteToReferee(Integer userId) {
        var user = this.verifyUserIsASpectator(userId);
        user.setRole(this.roleService.getRoleEntityByName("REFEREE"));
        this.userService.saveUserEntity(user);
    }

    private UserEntity verifyUserIsASpectator(Integer userId) {
        UserEntity user = this.userService.getUserEntityById(userId);
        if(!user.getRole().getName().equals("SPECTATOR")) {
            throw new UserNotASpectatorException("User with id " + userId + " is not a spectator");
        }
        return user;
    }
}