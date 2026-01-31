package com.moveit.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.notification.config.PaginationConfig;
import com.moveit.notification.dto.NotificationCreateDTO;
import com.moveit.notification.dto.NotificationResponseDTO;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.mapper.NotificationMapper;
import com.moveit.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationController.class)
@Import({ObjectMapper.class, PaginationConfig.class})
@DisplayName("NotificationController ")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;
    
    @MockitoBean
    private NotificationMapper notificationMapper;

    @Test
    @DisplayName("GET /notifications should return paginated notifications")
    void testGetNotifications() throws Exception {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setTitle("Test Notification");
        notification.setContent("Test Content");
        notification.setNotificationType(NotificationType.INCIDENT);
        notification.setCreatedAt(LocalDateTime.now());

        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(1L);
        dto.setTitle("Test Notification");
        dto.setContent("Test Content");
        dto.setNotificationType(NotificationType.INCIDENT);
        dto.setCreatedAt(LocalDateTime.now());

        Page<Notification> page = new PageImpl<>(Arrays.asList(notification), PageRequest.of(0, 10), 1);
        when(notificationService.getNotifications(any(), any(), any(), any())).thenReturn(page);
        when(notificationMapper.toResponseDTO(notification)).thenReturn(dto);

        mockMvc.perform(get("/notifications")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test Notification"))
                .andExpect(jsonPath("$.content[0].content").value("Test Content"));

        verify(notificationService, times(1)).getNotifications(any(), any(), any(), any());
    }

    @Test
    @DisplayName("GET /notifications with filters should apply filters")
    void testGetNotificationsWithFilters() throws Exception {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setTitle("Incident Notification");
        notification.setNotificationType(NotificationType.INCIDENT);
        notification.setIncidentIds(new HashSet<>(Set.of(100L)));

        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(1L);
        dto.setTitle("Incident Notification");
        dto.setNotificationType(NotificationType.INCIDENT);
        dto.setIncidentIds(new HashSet<>(Set.of(100L)));

        Page<Notification> page = new PageImpl<>(Arrays.asList(notification), PageRequest.of(0, 10), 1);
        when(notificationService.getNotifications(any(), any(), any(), any())).thenReturn(page);
        when(notificationMapper.toResponseDTO(notification)).thenReturn(dto);

        mockMvc.perform(get("/notifications")
                        .param("type", "INCIDENT")
                        .param("incidentId", "100")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].notificationType").value("INCIDENT"));

        verify(notificationService, times(1)).getNotifications(any(), any(), any(), any());
    }

    @Test
    @DisplayName("GET /notifications/{id} should return notification when found")
    void testGetNotificationById() throws Exception {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setTitle("Test Notification");
        notification.setContent("Test Content");
        notification.setNotificationType(NotificationType.INCIDENT);

        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(1L);
        dto.setTitle("Test Notification");
        dto.setContent("Test Content");

        when(notificationService.getNotificationById(1L)).thenReturn(Optional.of(notification));
        when(notificationMapper.toResponseDTO(notification)).thenReturn(dto);

        mockMvc.perform(get("/notifications/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Notification"))
                .andExpect(jsonPath("$.content").value("Test Content"));

        verify(notificationService, times(1)).getNotificationById(1L);
    }

    @Test
    @DisplayName("GET /notifications/{id} should return 404 when not found")
    void testGetNotificationById_NotFound() throws Exception {
        when(notificationService.getNotificationById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/notifications/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(notificationService, times(1)).getNotificationById(999L);
    }

    @Test
    @DisplayName("POST /notifications should create notification")
    void testCreateNotification() throws Exception {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setTitle("New Notification");
        notification.setContent("New Content");
        notification.setNotificationType(NotificationType.EVENT);

        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(1L);
        dto.setTitle("New Notification");

        NotificationCreateDTO createDTO = new NotificationCreateDTO();
        createDTO.setTitle("New Notification");
        createDTO.setContent("New Content");
        createDTO.setNotificationType(NotificationType.EVENT);

        when(notificationService.createNotification(any(NotificationCreateDTO.class))).thenReturn(notification);
        when(notificationMapper.toResponseDTO(notification)).thenReturn(dto);

        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Notification"));

        verify(notificationService, times(1)).createNotification(any(NotificationCreateDTO.class));
    }

    @Test
    @DisplayName("PUT /notifications/{id} should update notification when found")
    void testUpdateNotification() throws Exception {
        Notification updatedNotification = new Notification();
        updatedNotification.setId(1L);
        updatedNotification.setTitle("Updated Title");
        updatedNotification.setContent("Updated Content");
        updatedNotification.setNotificationType(NotificationType.INCIDENT);

        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(1L);
        dto.setTitle("Updated Title");
        dto.setContent("Updated Content");

        when(notificationService.updateNotification(eq(1L), any())).thenReturn(Optional.of(updatedNotification));
        when(notificationMapper.toResponseDTO(updatedNotification)).thenReturn(dto);

        mockMvc.perform(put("/notifications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Title\",\"content\":\"Updated Content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"));

        verify(notificationService, times(1)).updateNotification(eq(1L), any());
    }

    @Test
    @DisplayName("PUT /notifications/{id} should return 404 when not found")
    void testUpdateNotification_NotFound() throws Exception {
        when(notificationService.updateNotification(eq(999L), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/notifications/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Title\"}"))
                .andExpect(status().isNotFound());

        verify(notificationService, times(1)).updateNotification(eq(999L), any());
    }

    @Test
    @DisplayName("DELETE /notifications/{id} should delete notification")
    void testDeleteNotification() throws Exception {
        doNothing().when(notificationService).deleteNotification(1L);

        mockMvc.perform(delete("/notifications/1"))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).deleteNotification(1L);
    }
}
