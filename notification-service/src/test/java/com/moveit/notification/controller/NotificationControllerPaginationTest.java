package com.moveit.notification.controller;

import com.moveit.notification.config.PaginationConfig;
import com.moveit.notification.mapper.NotificationMapper;
import com.moveit.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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

/**
 * Tests pour Point 10 - Pagination defaults et limites.
 * Valide que les paramètres de pagination sont correctement validés.
 */
@WebMvcTest(controllers = NotificationController.class)
@Import(PaginationConfig.class)
@DisplayName("NotificationController - Pagination Limits (Point 10)")
class NotificationControllerPaginationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;
    
    @MockitoBean
    private NotificationMapper notificationMapper;

    @Test
    @DisplayName("GET /notifications with valid page=0, size=10 should succeed")
    void testGetNotifications_DefaultPagination() throws Exception {
        when(notificationService.getNotifications(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /notifications with custom valid page=2, size=50 should succeed")
    void testGetNotifications_CustomValidPagination() throws Exception {
        when(notificationService.getNotifications(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/notifications")
                        .param("page", "2")
                        .param("size", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /notifications with max page size (100) should succeed")
    void testGetNotifications_MaxPageSize() throws Exception {
        when(notificationService.getNotifications(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/notifications")
                        .param("size", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /notifications with size exceeding max (101) should return 400")
    void testGetNotifications_SizeExceedsMax() throws Exception {
        mockMvc.perform(get("/notifications")
                        .param("size", "101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Pagination"))
                .andExpect(jsonPath("$.message").value(
                    org.hamcrest.Matchers.containsString("Page size must be between")
                ));
    }

    @Test
    @DisplayName("GET /notifications with size=0 should return 400")
    void testGetNotifications_SizeZero() throws Exception {
        mockMvc.perform(get("/notifications")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Pagination"));
    }

    @Test
    @DisplayName("GET /notifications with negative page should return 400")
    void testGetNotifications_NegativePage() throws Exception {
        mockMvc.perform(get("/notifications")
                        .param("page", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Pagination"))
                .andExpect(jsonPath("$.message").value(
                    org.hamcrest.Matchers.containsString("Page number must be >= 0")
                ));
    }

    @Test
    @DisplayName("GET /notifications with very large size should return 400")
    void testGetNotifications_VeryLargeSize() throws Exception {
        mockMvc.perform(get("/notifications")
                        .param("size", "10000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Pagination"));
    }

    @Test
    @DisplayName("GET /notifications with negative size should return 400")
    void testGetNotifications_NegativeSize() throws Exception {
        mockMvc.perform(get("/notifications")
                        .param("size", "-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Pagination"));
    }
}
