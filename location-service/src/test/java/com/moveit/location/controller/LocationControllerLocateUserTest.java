package com.moveit.location.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.location.dto.LocateRequest;
import com.moveit.location.dto.LocateResponse;
import com.moveit.location.service.LocationService;
import com.moveit.location.service.LocationLocatorService;
import com.moveit.location.service.exception.UserServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LocationController.class)
@DisplayName("POST /locations/locate - Localisation d'utilisateurs")
class LocationControllerLocateUserTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private LocationLocatorService locatorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final LocateResponse OK_RESPONSE = new LocateResponse(48.8566, 2.3522);

    private void expectOk(LocateRequest req) throws Exception {
        when(locatorService.locate(any(LocateRequest.class), any())).thenReturn(OK_RESPONSE);
        performLocate(req).andExpect(status().isOk());
    }

    private void expectForbidden(LocateRequest req, String errorMessage) throws Exception {
        when(locatorService.locate(any(LocateRequest.class), any()))
                .thenThrow(new UserServiceException(errorMessage));
        performLocate(req).andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.ResultActions performLocate(LocateRequest req) throws Exception {
        return mockMvc.perform(post("/locations/locate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
    }

    @Nested
    @DisplayName("Admin - règles de localisation")
    class AdminLocateTests {

        @Test
        @DisplayName("Admin peut localiser un autre admin")
        void adminCanLocateAdmin() throws Exception {
            expectOk(new LocateRequest(10, 11));
        }

        @Test
        @DisplayName("Admin peut localiser un arbitre")
        void adminCanLocateReferee() throws Exception {
            expectOk(new LocateRequest(10, 3));
        }

        @Test
        @DisplayName("Admin peut localiser un athlète même sans consentement")
        void adminCanLocateAthlete() throws Exception {
            expectOk(new LocateRequest(10, 4));
        }

        @Test
        @DisplayName("Admin peut localiser un spectateur qui accepte le partage")
        void adminCanLocateConsentingSpectator() throws Exception {
            expectOk(new LocateRequest(10, 1));
        }

        @Test
        @DisplayName("Admin ne peut PAS localiser un spectateur qui refuse le partage")
        void adminCannotLocateNonConsentingSpectator() throws Exception {
            expectForbidden(new LocateRequest(10, 1),
                    "Le spectateur ciblé n'a pas accepté d'être localisé.");
        }
    }

    @Nested
    @DisplayName("Spectateur - règles de localisation")
    class SpectatorLocateTests {

        @Test
        @DisplayName("Spectateur peut localiser un spectateur qui a donné son consentement")
        void spectatorCanLocateSpectatorWithConsent() throws Exception {
            expectOk(new LocateRequest(1, 2));
        }

        @Test
        @DisplayName("Spectateur ne peut PAS localiser un spectateur sans consentement")
        void spectatorCannotLocateSpectatorWithoutConsent() throws Exception {
            expectForbidden(new LocateRequest(1, 1),
                    "Le spectateur ciblé n'a pas accepté d'être localisé.");
        }

        @Test
        @DisplayName("Spectateur ne peut PAS localiser un athlète")
        void spectatorCannotLocateAthlete() throws Exception {
            expectForbidden(new LocateRequest(1, 4),
                    "Non autorisé à localiser cet utilisateur.");
        }

        @Test
        @DisplayName("Spectateur ne peut PAS localiser un arbitre")
        void spectatorCannotLocateReferee() throws Exception {
            expectForbidden(new LocateRequest(1, 3),
                    "Non autorisé à localiser cet utilisateur.");
        }

        @Test
        @DisplayName("Spectateur ne peut PAS localiser un admin")
        void spectatorCannotLocateAdmin() throws Exception {
            expectForbidden(new LocateRequest(1, 10),
                    "Non autorisé à localiser cet utilisateur.");
        }
    }

    @Nested
    @DisplayName("Arbitre - règles de localisation")
    class RefereeLocateTests {

        @Test
        @DisplayName("Arbitre peut localiser un athlète même sans consentement")
        void refereeCanLocateAthlete() throws Exception {
            expectOk(new LocateRequest(3, 4));
        }

        @Test
        @DisplayName("Arbitre peut localiser un spectateur qui accepte le partage")
        void refereeCanLocateConsentingSpectator() throws Exception {
            expectOk(new LocateRequest(3, 1));
        }

        @Test
        @DisplayName("Arbitre ne peut PAS localiser un spectateur qui refuse le partage")
        void refereeCannotLocateNonConsentingSpectator() throws Exception {
            expectForbidden(new LocateRequest(3, 1),
                    "Le spectateur ciblé n'a pas accepté d'être localisé.");
        }

        @Test
        @DisplayName("Arbitre ne peut PAS localiser un autre arbitre")
        void refereeCannotLocateReferee() throws Exception {
            expectForbidden(new LocateRequest(3, 3),
                    "Non autorisé à localiser cet utilisateur.");
        }

        @Test
        @DisplayName("Arbitre ne peut PAS localiser un admin")
        void refereeCannotLocateAdmin() throws Exception {
            expectForbidden(new LocateRequest(3, 10),
                    "Non autorisé à localiser cet utilisateur.");
        }
    }

    @Nested
    @DisplayName("Athlète - ne peut localiser personne")
    class AthleteLocateTests {

        @Test
        @DisplayName("Athlète ne peut PAS localiser un autre athlète")
        void athleteCannotLocateAthlete() throws Exception {
            expectForbidden(new LocateRequest(4, 4),
                    "Non autorisé à localiser cet utilisateur.");
        }

        @Test
        @DisplayName("Athlète ne peut PAS localiser un spectateur")
        void athleteCannotLocateSpectator() throws Exception {
            expectForbidden(new LocateRequest(4, 1),
                    "Non autorisé à localiser cet utilisateur.");
        }

        @Test
        @DisplayName("Athlète ne peut PAS localiser un arbitre")
        void athleteCannotLocateReferee() throws Exception {
            expectForbidden(new LocateRequest(4, 3),
                    "Non autorisé à localiser cet utilisateur.");
        }

        @Test
        @DisplayName("Athlète ne peut PAS localiser un admin")
        void athleteCannotLocateAdmin() throws Exception {
            expectForbidden(new LocateRequest(4, 10),
                    "Non autorisé à localiser cet utilisateur.");
        }
    }
}