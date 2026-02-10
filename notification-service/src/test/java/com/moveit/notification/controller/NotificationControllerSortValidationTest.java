package com.moveit.notification.controller;

import com.moveit.notification.config.PaginationConfig;
import com.moveit.notification.mapper.NotificationMapper;
import com.moveit.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import(PaginationConfig.class)
@DisplayName("NotificationController - Sort Validation (Point 7)")
class NotificationControllerSortValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;
    
    @MockitoBean
    private NotificationMapper notificationMapper;

    @ParameterizedTest
    @ValueSource(strings = {"createdAt", "id", "title", "notificationType"})
    @DisplayName("GET /notifications with valid sortBy should succeed")
    void testGetNotifications_ValidSortFields(String sortField) throws Exception {
        Page<Object> emptyPage = new PageImpl<>(Collections.emptyList());
        when(notificationService.getNotifications(any(), any(), any(), any())).thenReturn((Page) emptyPage);

        mockMvc.perform(get("/notifications")
                        .param("sortBy", sortField)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /notifications with invalid sortBy should return 400 Bad Request")
    void testGetNotifications_InvalidSortField() throws Exception {
        mockMvc.perform(get("/notifications")
                        .param("sortBy", "maliciousField")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Sort Field"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid sort field: 'maliciousField'")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Allowed fields:")));
    }

    @Test
    @DisplayName("GET /notifications with SQL injection attempt should return 400")
    void testGetNotifications_SQLInjectionAttempt() throws Exception {
        mockMvc.perform(get("/notifications")
                        .param("sortBy", "id; DROP TABLE notifications--")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Sort Field"));
    }

    @Test
    @DisplayName("GET /notifications with unknown column should return 400")
    void testGetNotifications_UnknownColumn() throws Exception {
        mockMvc.perform(get("/notifications")
                        .param("sortBy", "unknownColumn")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Sort Field"));
    }
}
