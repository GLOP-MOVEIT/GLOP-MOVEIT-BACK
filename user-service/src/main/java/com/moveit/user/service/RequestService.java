package com.moveit.user.service;

import com.moveit.user.dto.Request;
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

    public Page<Request> getAllRequests(Pageable pageable) {
        return this.requestRepository.findAll(pageable)
                .map(requestMapper::toDto);
    }

    public Request getRequestById(Integer id) {
        return this.requestRepository.findById(id)
                .map(requestMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Request not found"));
    }

    public Request createAthleteRequest(Integer id) {
        return null;
    }

    public Request createVolunteerRequest(Integer id) {
        return null;
    }
}