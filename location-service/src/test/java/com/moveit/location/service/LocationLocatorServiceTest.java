package com.moveit.location.service;

import com.moveit.location.dto.*;
import com.moveit.location.service.exception.UserServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationLocatorService - Tests unitaires")
class LocationLocatorServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LocationLocatorService locationLocatorService;

    private static final String BASE_URL = "http://localhost:8086";
    private static final String CHAMPIONSHIP_BASE_URL = "http://localhost:8082";
    private static final String AUTH_HEADER = "Bearer test-token";

    @BeforeEach
    void setUp() {
        setField(locationLocatorService, "userServiceBaseUrl", BASE_URL);
        setField(locationLocatorService, "championshipServiceBaseUrl", CHAMPIONSHIP_BASE_URL);
    }

    private LocateRequest req(Integer requesterId, Integer targetId, Integer trialId) {
        LocateRequest r = new LocateRequest(requesterId, targetId);
        r.setTrialId(trialId);
        return r;
    }

    private LocateResponse locate(Integer requesterId, Integer targetId, Integer trialId, String authorizationHeader) {
        return locationLocatorService.locate(req(requesterId, targetId, trialId), authorizationHeader);
    }

    private BulkLocateTrialResponse locateAllForTrial(Integer requesterId, Integer trialId, String authorizationHeader) {
        BulkLocateTrialRequest request = new BulkLocateTrialRequest();
        request.setRequesterId(requesterId);
        request.setTrialId(trialId);
        return locationLocatorService.locateAllForTrial(request, authorizationHeader);
    }

    private UserDto createUser(Integer id, String roleName, boolean acceptsLocationSharing) {
        UserDto u = new UserDto();
        u.setUserId(id);
        u.setFirstName("First");
        u.setSurname("Last");
        u.setEmail("email@test.com");
        u.setPhoneNumber("0600000000");
        u.setLanguage("fr");
        u.setRole(new RoleDto(roleName));
        u.setAcceptsNotifications(true);
        u.setAcceptsLocationSharing(acceptsLocationSharing);
        return u;
    }

    private UserDto createUserWithNullRole(Integer id) {
        UserDto u = createUser(id, "SPECTATOR", true);
        u.setRole(null);
        return u;
    }

    private UserDto createUserWithNullRoleName(Integer id) {
        UserDto u = createUser(id, "SPECTATOR", true);
        u.setRole(new RoleDto(null));
        return u;
    }

    private void mockUserFetch(Integer userId, UserDto user) {
        lenient().when(restTemplate.exchange(
                eq(BASE_URL + "/users/" + userId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserDto.class)
        )).thenReturn(ResponseEntity.ok(user));
    }

    private void mockTrialFetch(Integer trialId, TrialDto trial) {
        lenient().when(restTemplate.exchange(
                eq(CHAMPIONSHIP_BASE_URL + "/trials/" + trialId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(TrialDto.class)
        )).thenReturn(ResponseEntity.ok(trial));
    }

    @Nested
    @DisplayName("Admin - règles de localisation")
    class AdminTests {

        @Test
        @DisplayName("Admin peut localiser un spectateur qui accepte le partage")
        void adminCanLocateConsentingSpectator() {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(2, createUser(2, "SPECTATOR", true));

            LocateResponse response = locationLocatorService.locate(req(1, 2, null), AUTH_HEADER);

            assertThat(response).isNotNull();
            assertThat(response.getLatitude()).isBetween(48.85, 48.88);
            assertThat(response.getLongitude()).isBetween(2.35, 2.38);
        }

        @Test
        @DisplayName("Admin ne peut PAS localiser un spectateur qui refuse le partage")
        void adminCannotLocateNonConsentingSpectator() {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(2, createUser(2, "SPECTATOR", false));

            assertThatThrownBy(() -> locate(1, 2, null, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("accepté");
        }

        @ParameterizedTest(name = "Admin peut localiser le rôle {0}")
        @CsvSource({
            "ATHLETE,2",
            "REFEREE,2",
            "ADMIN,2"
        })
        void adminCanLocateAllowedRoles(String targetRole, Integer targetId) {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(targetId, createUser(targetId, targetRole, false));

            LocateResponse response = locate(1, targetId, null, AUTH_HEADER);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Admin peut localiser un volontaire uniquement si le volontaire est sur le trial fourni")
        void adminCanLocateVolunteerOnlyOnSameTrial() {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(99, createUser(99, "VOLUNTEER", false));

            TrialDto t = new TrialDto();
            t.setTrialId(10);
            t.setCompetitionId(1);
            t.setParticipantIds(List.of(99));
            mockTrialFetch(10, t);

            LocateResponse response = locationLocatorService.locate(req(1, 99, 10), AUTH_HEADER);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Admin ne peut PAS localiser un volontaire si pas sur le trial")
        void adminCannotLocateVolunteerIfNotOnTrial() {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(99, createUser(99, "VOLUNTEER", false));

            TrialDto t = new TrialDto();
            t.setTrialId(10);
            t.setCompetitionId(1);
            t.setParticipantIds(List.of(1, 2, 3));
            mockTrialFetch(10, t);

                assertThatThrownBy(() -> locate(1, 99, 10, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("même épreuve");
        }

        @Test
        @DisplayName("Admin ne peut PAS localiser un volontaire sans trialId")
        void adminCannotLocateVolunteerWithoutTrialId() {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(99, createUser(99, "VOLUNTEER", false));

            assertThatThrownBy(() -> locate(1, 99, null, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("trialId requis");
        }
    }

    @Nested
    @DisplayName("Spectateur - règles de localisation")
    class SpectatorTests {

        @Test
        @DisplayName("Spectateur peut localiser un spectateur qui accepte le partage")
        void spectatorCanLocateConsentingSpectator() {
            mockUserFetch(1, createUser(1, "SPECTATOR", true));
            mockUserFetch(2, createUser(2, "SPECTATOR", true));

            LocateResponse response = locationLocatorService.locate(req(1, 2, null), AUTH_HEADER);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Spectateur ne peut PAS localiser un spectateur qui refuse le partage")
        void spectatorCannotLocateNonConsentingSpectator() {
            mockUserFetch(1, createUser(1, "SPECTATOR", true));
            mockUserFetch(2, createUser(2, "SPECTATOR", false));

            assertThatThrownBy(() -> locate(1, 2, null, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("accepté");
        }

        @ParameterizedTest(name = "Spectateur ne peut PAS localiser le rôle {0}")
        @CsvSource({
            "ATHLETE,2,-1",
            "REFEREE,2,-1",
            "VOLUNTEER,99,10"
        })
        void spectatorCannotLocateForbiddenRoles(String targetRole, Integer targetId, Integer trialId) {
            mockUserFetch(1, createUser(1, "SPECTATOR", true));
            mockUserFetch(targetId, createUser(targetId, targetRole, false));

            Integer normalizedTrialId = (trialId != null && trialId < 0) ? null : trialId;

            assertThatThrownBy(() -> locate(1, targetId, normalizedTrialId, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Non autorisé");
        }
    }

    @Nested
    @DisplayName("Arbitre - règles de localisation")
    class RefereeTests {

        @Test
        @DisplayName("Arbitre peut localiser un athlète même sans consentement")
        void refereeCanLocateAthlete() {
            mockUserFetch(1, createUser(1, "REFEREE", false));
            mockUserFetch(2, createUser(2, "ATHLETE", false));

            LocateResponse response = locationLocatorService.locate(req(1, 2, null), AUTH_HEADER);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Arbitre peut localiser un spectateur qui accepte le partage")
        void refereeCanLocateConsentingSpectator() {
            mockUserFetch(1, createUser(1, "REFEREE", false));
            mockUserFetch(2, createUser(2, "SPECTATOR", true));

            LocateResponse response = locationLocatorService.locate(req(1, 2, null), AUTH_HEADER);

            assertThat(response).isNotNull();
        }

        @ParameterizedTest(name = "Arbitre ne peut PAS localiser un {0}")
        @CsvSource({
            "SPECTATOR, false, accepté",
            "REFEREE,   false, Non autorisé",
            "ADMIN,     false, Non autorisé"
        })
        void refereeCannotLocateForbiddenTargets(String targetRole, boolean acceptsSharing, String expectedMessage) {
            mockUserFetch(1, createUser(1, "REFEREE", false));
            mockUserFetch(2, createUser(2, targetRole, acceptsSharing));

            assertThatThrownBy(() -> locate(1, 2, null, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining(expectedMessage);
        }

        @Test
        @DisplayName("Arbitre peut localiser un volontaire uniquement si le volontaire est sur le trial fourni")
        void refereeCanLocateVolunteerOnlyOnSameTrial() {
            mockUserFetch(1, createUser(1, "REFEREE", false));
            mockUserFetch(99, createUser(99, "VOLUNTEER", false));

            TrialDto t = new TrialDto();
            t.setTrialId(10);
            t.setCompetitionId(1);
            t.setParticipantIds(List.of(99, 4));
            mockTrialFetch(10, t);

            LocateResponse response = locationLocatorService.locate(req(1, 99, 10), AUTH_HEADER);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Arbitre ne peut PAS localiser un volontaire si pas sur le trial")
        void refereeCannotLocateVolunteerIfNotOnTrial() {
            mockUserFetch(1, createUser(1, "REFEREE", false));
            mockUserFetch(99, createUser(99, "VOLUNTEER", false));

            TrialDto t = new TrialDto();
            t.setTrialId(10);
            t.setCompetitionId(1);
            t.setParticipantIds(List.of(1, 2, 3));
            mockTrialFetch(10, t);

                assertThatThrownBy(() -> locate(1, 99, 10, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("même épreuve");
        }
    }

    @Nested
    @DisplayName("Athlète - ne peut localiser personne")
    class AthleteTests {

        @Test
        @DisplayName("Athlète ne peut PAS localiser un autre athlète")
        void athleteCannotLocateAthlete() {
            mockUserFetch(1, createUser(1, "ATHLETE", false));
            mockUserFetch(2, createUser(2, "ATHLETE", false));

            assertThatThrownBy(() -> locate(1, 2, null, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Non autorisé");
        }

        @Test
        @DisplayName("Athlète ne peut PAS localiser un spectateur")
        void athleteCannotLocateSpectator() {
            mockUserFetch(1, createUser(1, "ATHLETE", false));
            mockUserFetch(2, createUser(2, "SPECTATOR", true));

            assertThatThrownBy(() -> locate(1, 2, null, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Non autorisé");
        }
    }

    @Nested
    @DisplayName("Cas d'erreur")
    class ErrorTests {

        @Test
        @DisplayName("Doit lever une exception si le rôle du requester est null")
        void shouldThrowWhenRequesterRoleNull() {
            UserDto requester = createUserWithNullRole(1);
            mockUserFetch(1, requester);
            mockUserFetch(2, createUser(2, "SPECTATOR", true));

            assertThatThrownBy(() -> locate(1, 2, null, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Impossible de vérifier");
        }

        @Test
        @DisplayName("Doit lever une exception si le rôle du target est null")
        void shouldThrowWhenTargetRoleNull() {
            mockUserFetch(1, createUser(1, "SPECTATOR", true));
            UserDto target = createUserWithNullRole(2);
            mockUserFetch(2, target);

            assertThatThrownBy(() -> locate(1, 2, null, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Impossible de vérifier");
        }

        @Test
        @DisplayName("Doit lever une exception si le requester est null (body vide)")
        void shouldThrowWhenRequesterIsNull() {
            when(restTemplate.exchange(
                    eq(BASE_URL + "/users/1"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(UserDto.class)
            )).thenReturn(ResponseEntity.ok(null));
            mockUserFetch(2, createUser(2, "SPECTATOR", true));

            assertThatThrownBy(() -> locate(1, 2, null, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Impossible de vérifier");
        }

        @Test
        @DisplayName("Doit lever une exception si le target est null (body vide)")
        void shouldThrowWhenTargetIsNull() {
            mockUserFetch(1, createUser(1, "SPECTATOR", true));
            when(restTemplate.exchange(
                    eq(BASE_URL + "/users/2"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(UserDto.class)
            )).thenReturn(ResponseEntity.ok(null));

            assertThatThrownBy(() -> locate(1, 2, null, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Impossible de vérifier");
        }

        @Test
        @DisplayName("Doit lever une exception si le nom du rôle du requester est null")
        void shouldThrowWhenRequesterRoleNameNull() {
            UserDto requester = createUserWithNullRoleName(1);
            mockUserFetch(1, requester);
            mockUserFetch(2, createUser(2, "SPECTATOR", true));

            assertThatThrownBy(() -> locate(1, 2, null, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Impossible de vérifier");
        }

        @Test
        @DisplayName("Doit lever une exception si le service utilisateur est injoignable")
        void shouldThrowWhenUserServiceUnreachable() {
            when(restTemplate.exchange(
                    eq(BASE_URL + "/users/1"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(UserDto.class)
            )).thenThrow(new RuntimeException("Connection refused"));

            assertThatThrownBy(() -> locate(1, 2, null, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Impossible de récupérer");
        }

        @ParameterizedTest(name = "Authorization header absent ou vide")
        @NullSource
        @ValueSource(strings = {"   "})
        void shouldHandleAuthorizationHeaderWithoutForwarding(String authorizationHeader) {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(2, createUser(2, "ATHLETE", false));

            LocateResponse response = locate(1, 2, null, authorizationHeader);

            assertThat(response).isNotNull();

            verify(restTemplate, never()).exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    argThat(entity -> AUTH_HEADER.equals(entity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))),
                    eq(UserDto.class)
            );
        }

        @Test
        @DisplayName("Doit transmettre le header Authorization quand il est présent")
        void shouldForwardAuthorizationHeader() {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(2, createUser(2, "ATHLETE", false));

            LocateResponse response = locate(1, 2, null, AUTH_HEADER);

            assertThat(response).isNotNull();
            verify(restTemplate, times(2)).exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    argThat(entity -> AUTH_HEADER.equals(entity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))),
                    eq(UserDto.class)
            );
        }
    }

    @Nested
    @DisplayName("Localisation en masse par épreuve (trial)")
    class BulkLocateTrialTests {

        @Test
        @DisplayName("Referee peut récupérer les positions (athlètes + volontaires) avec les noms")
        void refereeCanBulkLocateTrialParticipants() {
            mockUserFetch(1, createUser(1, "REFEREE", false));

            UserDto athlete = new UserDto();
            athlete.setUserId(2);
            athlete.setFirstName("Alice");
            athlete.setSurname("Athlete");
            athlete.setEmail("a@test.com");
            athlete.setPhoneNumber("0600000000");
            athlete.setLanguage("fr");
            athlete.setRole(new RoleDto("ATHLETE"));
            athlete.setAcceptsNotifications(true);
            athlete.setAcceptsLocationSharing(false);

            UserDto volunteer = new UserDto();
            volunteer.setUserId(3);
            volunteer.setFirstName("Victor");
            volunteer.setSurname("Volunteer");
            volunteer.setEmail("v@test.com");
            volunteer.setPhoneNumber("0600000000");
            volunteer.setLanguage("fr");
            volunteer.setRole(new RoleDto("VOLUNTEER"));
            volunteer.setAcceptsNotifications(true);
            volunteer.setAcceptsLocationSharing(false);

            UserDto spectator = new UserDto();
            spectator.setUserId(4);
            spectator.setFirstName("Sam");
            spectator.setSurname("Spectator");
            spectator.setEmail("s@test.com");
            spectator.setPhoneNumber("0600000000");
            spectator.setLanguage("fr");
            spectator.setRole(new RoleDto("SPECTATOR"));
            spectator.setAcceptsNotifications(true);
            spectator.setAcceptsLocationSharing(true);

            mockUserFetch(2, athlete);
            mockUserFetch(3, volunteer);
            mockUserFetch(4, spectator);

            TrialDto t = new TrialDto();
            t.setTrialId(10);
            t.setCompetitionId(1);
            t.setParticipantIds(List.of(2, 3, 4));
            mockTrialFetch(10, t);

            BulkLocateTrialRequest r = new BulkLocateTrialRequest();
            r.setRequesterId(1);
            r.setTrialId(10);

            BulkLocateTrialResponse response = locationLocatorService.locateAllForTrial(r, AUTH_HEADER);

            assertThat(response).isNotNull();
            assertThat(response.getTrialId()).isEqualTo(10);
            assertThat(response.getAthletes()).hasSize(1);
            assertThat(response.getVolunteers()).hasSize(1);

            BulkLocateUserPosition athletePos = response.getAthletes().get(0);
            assertThat(athletePos.getUserId()).isEqualTo(2);
            assertThat(athletePos.getFirstName()).isEqualTo("Alice");
            assertThat(athletePos.getSurname()).isEqualTo("Athlete");

            BulkLocateUserPosition volunteerPos = response.getVolunteers().get(0);
            assertThat(volunteerPos.getUserId()).isEqualTo(3);
            assertThat(volunteerPos.getFirstName()).isEqualTo("Victor");
            assertThat(volunteerPos.getSurname()).isEqualTo("Volunteer");
        }

        @Test
        @DisplayName("Non-referee/non-admin ne peut pas bulk locate")
        void spectatorCannotBulkLocate() {
            mockUserFetch(1, createUser(1, "SPECTATOR", true));

            TrialDto t = new TrialDto();
            t.setTrialId(10);
            t.setCompetitionId(1);
            t.setParticipantIds(List.of());
            mockTrialFetch(10, t);

                assertThatThrownBy(() -> locateAllForTrial(1, 10, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Non autorisé");
        }

        @Test
        @DisplayName("Bulk locate doit exiger trialId")
        void bulkLocateRequiresTrialId() {
            mockUserFetch(1, createUser(1, "REFEREE", false));

            assertThatThrownBy(() -> locateAllForTrial(1, null, AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("trialId");
        }
    }
}
