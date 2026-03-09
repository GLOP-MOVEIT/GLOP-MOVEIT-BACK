package com.moveit.location.service;

import com.moveit.location.dto.*;
import com.moveit.location.service.exception.UserServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        LocateRequest r = new LocateRequest();
        r.setRequesterId(requesterId);
        r.setTargetId(targetId);
        r.setTrialId(trialId);
        return r;
    }

    private UserDto createUser(Integer id, String roleName, boolean acceptsLocationSharing) {
        return new UserDto(id, "First", "Last", "email@test.com",
                "0600000000", "fr", new RoleDto(roleName),
                true, acceptsLocationSharing);
    }

    private UserDto createUserWithNullRole(Integer id) {
        return new UserDto(id, "First", "Last", "email@test.com",
                "0600000000", "fr", null,
                true, true);
    }

    private UserDto createUserWithNullRoleName(Integer id) {
        return new UserDto(id, "First", "Last", "email@test.com",
                "0600000000", "fr", new RoleDto(null),
                true, true);
    }

    private void mockUserFetch(Integer userId, UserDto user) {
        when(restTemplate.exchange(
                eq(BASE_URL + "/users/" + userId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserDto.class)
        )).thenReturn(ResponseEntity.ok(user));
    }

    private void mockTrialFetch(Integer trialId, TrialDto trial) {
        when(restTemplate.exchange(
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

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 2, null), AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("accepté");
        }

        @Test
        @DisplayName("Admin peut localiser un athlète même sans consentement")
        void adminCanLocateAthlete() {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(2, createUser(2, "ATHLETE", false));

            LocateResponse response = locationLocatorService.locate(req(1, 2, null), AUTH_HEADER);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Admin peut localiser un arbitre")
        void adminCanLocateReferee() {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(2, createUser(2, "REFEREE", false));

            LocateResponse response = locationLocatorService.locate(req(1, 2, null), AUTH_HEADER);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Admin peut localiser un autre admin")
        void adminCanLocateAdmin() {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(2, createUser(2, "ADMIN", false));

            LocateResponse response = locationLocatorService.locate(req(1, 2, null), AUTH_HEADER);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Admin peut localiser un volontaire uniquement si le volontaire est sur le trial fourni")
        void adminCanLocateVolunteerOnlyOnSameTrial() {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(99, createUser(99, "VOLUNTEER", false));
            mockTrialFetch(10, new TrialDto(10, 1, List.of(99)));

            LocateResponse response = locationLocatorService.locate(req(1, 99, 10), AUTH_HEADER);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Admin ne peut PAS localiser un volontaire si pas sur le trial")
        void adminCannotLocateVolunteerIfNotOnTrial() {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(99, createUser(99, "VOLUNTEER", false));
            mockTrialFetch(10, new TrialDto(10, 1, List.of(1, 2, 3)));

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 99, 10), AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("même épreuve");
        }

        @Test
        @DisplayName("Admin ne peut PAS localiser un volontaire sans trialId")
        void adminCannotLocateVolunteerWithoutTrialId() {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(99, createUser(99, "VOLUNTEER", false));

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 99, null), AUTH_HEADER))
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

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 2, null), AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("accepté");
        }

        @Test
        @DisplayName("Spectateur ne peut PAS localiser un athlète")
        void spectatorCannotLocateAthlete() {
            mockUserFetch(1, createUser(1, "SPECTATOR", true));
            mockUserFetch(2, createUser(2, "ATHLETE", false));

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 2, null), AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Non autorisé");
        }

        @Test
        @DisplayName("Spectateur ne peut PAS localiser un arbitre")
        void spectatorCannotLocateReferee() {
            mockUserFetch(1, createUser(1, "SPECTATOR", true));
            mockUserFetch(2, createUser(2, "REFEREE", false));

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 2, null), AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Non autorisé");
        }

        @Test
        @DisplayName("Spectateur ne peut PAS localiser un volontaire")
        void spectatorCannotLocateVolunteer() {
            mockUserFetch(1, createUser(1, "SPECTATOR", true));
            mockUserFetch(99, createUser(99, "VOLUNTEER", false));

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 99, 10), AUTH_HEADER))
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

        @Test
        @DisplayName("Arbitre ne peut PAS localiser un spectateur qui refuse le partage")
        void refereeCannotLocateNonConsentingSpectator() {
            mockUserFetch(1, createUser(1, "REFEREE", false));
            mockUserFetch(2, createUser(2, "SPECTATOR", false));

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 2, null), AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("accepté");
        }

        @Test
        @DisplayName("Arbitre ne peut PAS localiser un autre arbitre")
        void refereeCannotLocateReferee() {
            mockUserFetch(1, createUser(1, "REFEREE", false));
            mockUserFetch(2, createUser(2, "REFEREE", false));

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 2, null), AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Non autorisé");
        }

        @Test
        @DisplayName("Arbitre ne peut PAS localiser un admin")
        void refereeCannotLocateAdmin() {
            mockUserFetch(1, createUser(1, "REFEREE", false));
            mockUserFetch(2, createUser(2, "ADMIN", false));

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 2, null), AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Non autorisé");
        }

        @Test
        @DisplayName("Arbitre peut localiser un volontaire uniquement si le volontaire est sur le trial fourni")
        void refereeCanLocateVolunteerOnlyOnSameTrial() {
            mockUserFetch(1, createUser(1, "REFEREE", false));
            mockUserFetch(99, createUser(99, "VOLUNTEER", false));
            mockTrialFetch(10, new TrialDto(10, 1, List.of(99, 4)));

            LocateResponse response = locationLocatorService.locate(req(1, 99, 10), AUTH_HEADER);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Arbitre ne peut PAS localiser un volontaire si pas sur le trial")
        void refereeCannotLocateVolunteerIfNotOnTrial() {
            mockUserFetch(1, createUser(1, "REFEREE", false));
            mockUserFetch(99, createUser(99, "VOLUNTEER", false));
            mockTrialFetch(10, new TrialDto(10, 1, List.of(1, 2, 3)));

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 99, 10), AUTH_HEADER))
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

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 2, null), AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Non autorisé");
        }

        @Test
        @DisplayName("Athlète ne peut PAS localiser un spectateur")
        void athleteCannotLocateSpectator() {
            mockUserFetch(1, createUser(1, "ATHLETE", false));
            mockUserFetch(2, createUser(2, "SPECTATOR", true));

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 2, null), AUTH_HEADER))
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

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 2, null), AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Impossible de vérifier");
        }

        @Test
        @DisplayName("Doit lever une exception si le rôle du target est null")
        void shouldThrowWhenTargetRoleNull() {
            mockUserFetch(1, createUser(1, "SPECTATOR", true));
            UserDto target = createUserWithNullRole(2);
            mockUserFetch(2, target);

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 2, null), AUTH_HEADER))
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

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 2, null), AUTH_HEADER))
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

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 2, null), AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Impossible de vérifier");
        }

        @Test
        @DisplayName("Doit lever une exception si le nom du rôle du requester est null")
        void shouldThrowWhenRequesterRoleNameNull() {
            UserDto requester = createUserWithNullRoleName(1);
            mockUserFetch(1, requester);
            mockUserFetch(2, createUser(2, "SPECTATOR", true));

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 2, null), AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Impossible de vérifier");
        }

        @Test
        @DisplayName("Doit lever une exception si le service utilisateur est injoignable")
        void shouldThrowWhenUserServiceUnreachable() {
            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(UserDto.class)
            )).thenThrow(new RuntimeException("Connection refused"));

            assertThatThrownBy(() -> locationLocatorService.locate(req(1, 2, null), AUTH_HEADER))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Impossible de récupérer");
        }

        @Test
        @DisplayName("Doit fonctionner sans header Authorization (null)")
        void shouldWorkWithoutAuthorizationHeader() {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(2, createUser(2, "ATHLETE", false));

            LocateResponse response = locationLocatorService.locate(req(1, 2, null), null);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Doit fonctionner avec un header Authorization vide (blank)")
        void shouldWorkWithBlankAuthorizationHeader() {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(2, createUser(2, "ATHLETE", false));

            LocateResponse response = locationLocatorService.locate(req(1, 2, null), "   ");

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Doit transmettre le header Authorization quand il est présent")
        void shouldForwardAuthorizationHeader() {
            mockUserFetch(1, createUser(1, "ADMIN", false));
            mockUserFetch(2, createUser(2, "ATHLETE", false));

            LocateResponse response = locationLocatorService.locate(req(1, 2, null), AUTH_HEADER);

            assertThat(response).isNotNull();
            verify(restTemplate, times(2)).exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    argThat(entity -> {
                        String authValue = entity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                        return AUTH_HEADER.equals(authValue);
                    }),
                    eq(UserDto.class)
            );
        }
    }
}

