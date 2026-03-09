package com.moveit.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationTypeController.class)
@Import(ObjectMapper.class)
@DisplayName("NotificationTypeController WebMvc Tests")
class NotificationTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /notification-types should return all notification types")
    void testGetAllTypes() throws Exception {
        mockMvc.perform(get("/notification-types")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0]").value("ASSIGNMENT"))
                .andExpect(jsonPath("$[1]").value("RESULT"))
                .andExpect(jsonPath("$[2]").value("SCHEDULE_CHANGE"))
                .andExpect(jsonPath("$[3]").value("REGISTRATION"))
                .andExpect(jsonPath("$[4]").value("CANCELLATION"))
                .andExpect(jsonPath("$[5]").value("REMINDER"))
                .andExpect(jsonPath("$[6]").value("START"))
                .andExpect(jsonPath("$[7]").value("ALERT"));
    }

    @Test
    @DisplayName("GET /notification-types/{type} should return specific type")
    void testGetType() throws Exception {
        mockMvc.perform(get("/notification-types/ASSIGNMENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("ASSIGNMENT"));
    }
}
