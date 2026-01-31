package com.moveit.notification.controller;

import com.moveit.notification.dto.NotificationCreateDTO;
import com.moveit.notification.dto.NotificationUpdateDTO;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController Unit Tests")
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private Notification notification;
    private NotificationCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId(1L);
        notification.setTitle("Test Notification");
        notification.setContent("Test Content");
        notification.setNotificationType(NotificationType.INCIDENT);
        notification.setIncidentIds(new HashSet<>(Set.of(100L)));
        notification.setCreatedAt(LocalDateTime.now());

        createDTO = new NotificationCreateDTO();
        createDTO.setTitle("New Notification");
        createDTO.setContent("New Content");
        createDTO.setNotificationType(NotificationType.EVENT);
        createDTO.setEventIds(new HashSet<>(Set.of(200L)));
    }

    @Test
    @DisplayName("getNotifications should return paginated notifications")
    void testGetNotifications() {
        Page<Notification> page = new PageImpl<>(Arrays.asList(notification), PageRequest.of(0, 10), 1);
        when(notificationService.getNotifications(any(), any(), any(), any())).thenReturn(page);

        ResponseEntity<Page<Notification>> response = notificationController.getNotifications(
                null, null, null, 0, 10, "createdAt", "DESC");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Test Notification", response.getBody().getContent().get(0).getTitle());
        verify(notificationService, times(1)).getNotifications(any(), any(), any(), any());
    }

    @Test
    @DisplayName("getNotifications with filters should apply filters")
    void testGetNotificationsWithFilters() {
        Page<Notification> page = new PageImpl<>(Arrays.asList(notification), PageRequest.of(0, 10), 1);
        when(notificationService.getNotifications(any(), any(), any(), any())).thenReturn(page);

        ResponseEntity<Page<Notification>> response = notificationController.getNotifications(
                NotificationType.INCIDENT, 100L, null, 0, 10, "createdAt", "DESC");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(notificationService, times(1)).getNotifications(any(), any(), any(), any());
    }

    @Test
    @DisplayName("getNotificationById should return notification when found")
    void testGetNotificationById_Found() {
        when(notificationService.getNotificationById(1L)).thenReturn(Optional.of(notification));

        ResponseEntity<Notification> response = notificationController.getNotificationById(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test Notification", response.getBody().getTitle());
        verify(notificationService, times(1)).getNotificationById(1L);
    }

    @Test
    @DisplayName("getNotificationById should return 404 when not found")
    void testGetNotificationById_NotFound() {
        when(notificationService.getNotificationById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Notification> response = notificationController.getNotificationById(999L);

        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        verify(notificationService, times(1)).getNotificationById(999L);
    }

    @Test
    @DisplayName("createNotification should create and return notification")
    void testCreateNotification() {
        when(notificationService.createNotification(any(NotificationCreateDTO.class))).thenReturn(notification);

        ResponseEntity<Notification> response = notificationController.createNotification(createDTO);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Test Notification", response.getBody().getTitle());
        verify(notificationService, times(1)).createNotification(any(NotificationCreateDTO.class));
    }

    @Test
    @DisplayName("updateNotification should update notification when found")
    void testUpdateNotification_Found() {
        NotificationUpdateDTO updateDTO = new NotificationUpdateDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setContent("Updated Content");

        Notification updated = new Notification();
        updated.setId(1L);
        updated.setTitle("Updated Title");
        updated.setContent("Updated Content");
        updated.setNotificationType(NotificationType.INCIDENT);

        when(notificationService.updateNotification(eq(1L), any(NotificationUpdateDTO.class)))
                .thenReturn(Optional.of(updated));

        ResponseEntity<Notification> response = notificationController.updateNotification(1L, updateDTO);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Updated Title", response.getBody().getTitle());
        verify(notificationService, times(1)).updateNotification(eq(1L), any(NotificationUpdateDTO.class));
    }

    @Test
    @DisplayName("updateNotification should return 404 when not found")
    void testUpdateNotification_NotFound() {
        NotificationUpdateDTO updateDTO = new NotificationUpdateDTO();
        updateDTO.setTitle("Updated Title");

        when(notificationService.updateNotification(eq(999L), any(NotificationUpdateDTO.class)))
                .thenReturn(Optional.empty());

        ResponseEntity<Notification> response = notificationController.updateNotification(999L, updateDTO);

        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        verify(notificationService, times(1)).updateNotification(eq(999L), any(NotificationUpdateDTO.class));
    }

    @Test
    @DisplayName("deleteNotification should delete notification")
    void testDeleteNotification() {
        doNothing().when(notificationService).deleteNotification(1L);

        ResponseEntity<Void> response = notificationController.deleteNotification(1L);

        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value());
        verify(notificationService, times(1)).deleteNotification(1L);
    }
}
