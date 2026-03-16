package com.moveit.location.service;

import com.moveit.location.dto.*;
import com.moveit.location.service.exception.UserServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationLocatorService {
    private final RestTemplate restTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${USER_SERVICE_URL:http://user-service:8086}")
    private String userServiceBaseUrl;

    @Value("${CHAMPIONSHIP_SERVICE_URL:http://championship-service:8082}")
    private String championshipServiceBaseUrl;

    public LocateResponse locate(LocateRequest request, String authorization) {
        UserDto requester = safeFetchUser(request.getRequesterId(), authorization);
        UserDto target = safeFetchUser(request.getTargetId(), authorization);
        verifyRolesAndConsent(requester, target, request);
        return buildLocateResponse();
    }

    public BulkLocateTrialResponse locateAllForTrial(BulkLocateTrialRequest request, String authorization) {
        if (request == null || request.getRequesterId() == null || request.getTrialId() == null) {
            throw new UserServiceException("requesterId et trialId sont requis.");
        }

        UserDto requester = safeFetchUser(request.getRequesterId(), authorization);
        String requesterRole = extractRole(requester);
        if (requesterRole == null) {
            throw new UserServiceException("Impossible de vérifier le rôle du demandeur.");
        }
        if (!isReferee(requesterRole) && !isAdmin(requesterRole)) {
            throw new UserServiceException("Non autorisé à localiser les participants d'une épreuve.");
        }

        TrialDto trial = safeFetchTrial(request.getTrialId(), authorization);
        List<Integer> participantIds = trial != null ? trial.getParticipantIds() : null;
        if (participantIds == null) participantIds = Collections.emptyList();

        List<BulkLocateUserPosition> athletes = new ArrayList<>();
        List<BulkLocateUserPosition> volunteers = new ArrayList<>();

        for (Integer participantId : participantIds) {
            if (participantId == null) continue;
            UserDto participant = safeFetchUser(participantId, authorization);
            String role = extractRole(participant);
            if (role == null) continue;

            if (isAthlete(role)) {
                LocateResponse pos = buildLocateResponse();
                athletes.add(new BulkLocateUserPosition(
                        participantId,
                        participant.getFirstName(),
                        participant.getSurname(),
                        pos.getLatitude(),
                        pos.getLongitude()
                ));
            } else if (isVolunteer(role)) {
                LocateResponse pos = buildLocateResponse();
                volunteers.add(new BulkLocateUserPosition(
                        participantId,
                        participant.getFirstName(),
                        participant.getSurname(),
                        pos.getLatitude(),
                        pos.getLongitude()
                ));
            }
        }

        return new BulkLocateTrialResponse(request.getTrialId(), athletes, volunteers);
    }

    private UserDto safeFetchUser(Integer userId, String authorization) {
        try {
            return fetchUser(userId, authorization);
        } catch (Exception e) {
            throw new UserServiceException("Impossible de récupérer l'utilisateur id=" + userId, e);
        }
    }

    private UserDto fetchUser(Integer userId, String authorization) {
        String url = userServiceBaseUrl + "/users/" + userId;
        HttpHeaders headers = new HttpHeaders();
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserDto.class);
        return response.getBody();
    }

    private TrialDto safeFetchTrial(Integer trialId, String authorization) {
        try {
            return fetchTrial(trialId, authorization);
        } catch (Exception e) {
            throw new UserServiceException("Impossible de récupérer la manche (trial) id=" + trialId, e);
        }
    }

    private TrialDto fetchTrial(Integer trialId, String authorization) {
        String url = championshipServiceBaseUrl + "/trials/" + trialId;
        HttpHeaders headers = new HttpHeaders();
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<TrialDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, TrialDto.class);
        return response.getBody();
    }

    private void verifyRolesAndConsent(UserDto requester, UserDto target, LocateRequest request) {
        String requesterRole = extractRole(requester);
        String targetRole = extractRole(target);

        validateRolesNotNull(requesterRole, targetRole);

        if (canAdminLocate(requesterRole, targetRole, target, request)) return;
        if (canRefereeLocate(requesterRole, targetRole, target, request)) return;
        if (canSpectatorLocateSpectator(requesterRole, targetRole, target)) return;

        throw new UserServiceException("Non autorisé à localiser cet utilisateur.");
    }

    private boolean canAdminLocate(String requesterRole, String targetRole, UserDto target, LocateRequest request) {
        if (!isAdmin(requesterRole)) return false;

        if (isVolunteer(targetRole)) {
            verifySameTrialForVolunteer(target, request);
            return true;
        }

        if (isSpectator(targetRole)) {
            verifyLocationSharingConsent(target);
        }
        return true;
    }

    private boolean canRefereeLocate(String requesterRole, String targetRole, UserDto target, LocateRequest request) {
        if (!isReferee(requesterRole)) return false;

        if (isVolunteer(targetRole)) {
            verifySameTrialForVolunteer(target, request);
            return true;
        }

        if (isAthlete(targetRole)) return true;
        if (isSpectator(targetRole)) {
            verifyLocationSharingConsent(target);
            return true;
        }
        return false;
    }

    private boolean canSpectatorLocateSpectator(String requesterRole, String targetRole, UserDto target) {
        if (!isSpectator(requesterRole) || !isSpectator(targetRole)) return false;
        verifyLocationSharingConsent(target);
        return true;
    }

    private void verifySameTrialForVolunteer(UserDto volunteerTarget, LocateRequest request) {
        if (request == null || request.getTrialId() == null) {
            throw new UserServiceException("trialId requis pour localiser un volontaire.");
        }

        TrialDto trial = safeFetchTrial(request.getTrialId(), null);
        if (trial == null || trial.getParticipantIds() == null) {
            throw new UserServiceException("Impossible de vérifier l'épreuve (trial) pour la localisation du volontaire.");
        }

        Integer volunteerId = volunteerTarget != null ? volunteerTarget.getUserId() : null;
        if (volunteerId == null) {
            throw new UserServiceException("Impossible de vérifier l'utilisateur ciblé.");
        }

        if (!trial.getParticipantIds().contains(volunteerId)) {
            throw new UserServiceException("Non autorisé à localiser ce volontaire: pas sur la même épreuve.");
        }
    }

    private String extractRole(UserDto user) {
        return user != null && user.getRole() != null ? user.getRole().getName() : null;
    }

    private void validateRolesNotNull(String requesterRole, String targetRole) {
        if (requesterRole == null || targetRole == null) {
            throw new UserServiceException("Impossible de vérifier les rôles");
        }
    }

    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    private boolean isReferee(String role) {
        return "REFEREE".equalsIgnoreCase(role);
    }

    private boolean isAthlete(String role) {
        return "ATHLETE".equalsIgnoreCase(role);
    }

    private boolean isSpectator(String role) {
        return "SPECTATOR".equalsIgnoreCase(role);
    }

    private boolean isVolunteer(String role) {
        return "VOLUNTEER".equalsIgnoreCase(role);
    }

    private void verifyLocationSharingConsent(UserDto target) {
        if (!target.isAcceptsLocationSharing()) {
            throw new UserServiceException("Le spectateur ciblé n'a pas accepté d'être localisé.");
        }
    }

    private LocateResponse buildLocateResponse() {
        double latitude = 48.8566 + secureRandom.nextDouble() * 0.02;
        double longitude = 2.3522 + secureRandom.nextDouble() * 0.02;
        return new LocateResponse(latitude, longitude);
    }
}
