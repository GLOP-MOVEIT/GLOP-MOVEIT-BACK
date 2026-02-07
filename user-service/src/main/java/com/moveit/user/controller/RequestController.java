package com.moveit.user.controller;

import com.moveit.user.dto.Request;
import com.moveit.user.service.RequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
@Tag(name = "Request", description = "API de gestion des demandes de promotion")
public class RequestController {

    private final RequestService requestService;

    @GetMapping
    public Page<Request> getAllRequests(Pageable pageable) {
        return this.requestService.getAllRequests(pageable);
    }

    @GetMapping("/{id}")
    public Request getRequestById(@PathVariable Integer id) {
        return this.requestService.getRequestById(id);
    }

    @PostMapping("/athlete/{id}")
    public Request requestToAthlete(@PathVariable Integer id) {
        return this.requestService.createAthleteRequest(id);
    }

    @PostMapping("/volunteer/{id}")
    public Request requestToVolunteer(@PathVariable Integer id) {
        return this.requestService.createVolunteerRequest(id);
    }
}