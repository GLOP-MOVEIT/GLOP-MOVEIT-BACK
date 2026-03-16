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
        validateBulkLocateTrialRequest(request);

        UserDto requester = safeFetchUser(request.getRequesterId(), authorization);
        ensureRequesterCanBulkLocateTrial(requester);

        List<Integer> participantIds = getTrialParticipantIds(request.getTrialId(), authorization);

        List<BulkLocateUserPosition> athletes = new ArrayList<>();
        List<BulkLocateUserPosition> volunteers = new ArrayList<>();
        addParticipantPositions(participantIds, authorization, athletes, volunteers);

        return new BulkLocateTrialResponse(request.getTrialId(), athletes, volunteers);
    }

    private void validateBulkLocateTrialRequest(BulkLocateTrialRequest request) {
        if (request == null || request.getRequesterId() == null || request.getTrialId() == null) {
            throw new UserServiceException("requesterId et trialId sont requis.");
        }
    }

    private void ensureRequesterCanBulkLocateTrial(UserDto requester) {
        String requesterRole = extractRole(requester);
        if (requesterRole == null) {
            throw new UserServiceException("Impossible de vérifier le rôle du demandeur.");
        }
        if (!isReferee(requesterRole) && !isAdmin(requesterRole)) {
            throw new UserServiceException("Non autorisé à localiser les participants d'une épreuve.");
        }
    }

    private List<Integer> getTrialParticipantIds(Integer trialId, String authorization) {
        TrialDto trial = safeFetchTrial(trialId, authorization);
        List<Integer> participantIds = trial != null ? trial.getParticipantIds() : null;
        return participantIds != null ? participantIds : Collections.emptyList();
    }

    private void addParticipantPositions(List<Integer> participantIds,
                                         String authorization,
                                         List<BulkLocateUserPosition> athletes,
                                         List<BulkLocateUserPosition> volunteers) {
        for (Integer participantId : participantIds) {
            if (participantId == null) continue;
            UserDto participant = safeFetchUser(participantId, authorization);
            String role = extractRole(participant);
            if (role == null) continue;

            if (isAthlete(role)) {
                athletes.add(buildUserPosition(participantId, participant));
            } else if (isVolunteer(role)) {
                volunteers.add(buildUserPosition(participantId, participant));
            }
        }
    }

    private BulkLocateUserPosition buildUserPosition(Integer userId, UserDto user) {
        LocateResponse pos = buildLocateResponse();
        String firstName = user != null ? user.getFirstName() : null;
        String surname = user != null ? user.getSurname() : null;
        return new BulkLocateUserPosition(userId, firstName, surname, pos.getLatitude(), pos.getLongitude());
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
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders(authorization));
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
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders(authorization));
        ResponseEntity<TrialDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, TrialDto.class);
        return response.getBody();
    }

    private HttpHeaders buildHeaders(String authorization) {
        HttpHeaders headers = new HttpHeaders();
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
        return headers;
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
