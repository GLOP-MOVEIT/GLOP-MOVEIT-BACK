package com.moveit.notification.controller;

import com.moveit.notification.entity.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationTypeController Unit Tests")
class NotificationTypeControllerTest {

    @InjectMocks
    private NotificationTypeController notificationTypeController;

    @Test
    @DisplayName("getAllTypes should return all notification types")
    void testGetAllTypes() {
        ResponseEntity<List<NotificationType>> response = notificationTypeController.getAllTypes();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().size());
        assertTrue(response.getBody().contains(NotificationType.INCIDENT));
        assertTrue(response.getBody().contains(NotificationType.EVENT));
        assertTrue(response.getBody().contains(NotificationType.SYSTEM));
        assertTrue(response.getBody().contains(NotificationType.MAINTENANCE));
        assertTrue(response.getBody().contains(NotificationType.ALERT));
    }

    @Test
    @DisplayName("getAllTypes should return all enum values in order")
    void testGetAllTypesOrder() {
        ResponseEntity<List<NotificationType>> response = notificationTypeController.getAllTypes();

        assertNotNull(response.getBody());
        List<NotificationType> expected = Arrays.asList(NotificationType.values());
        assertEquals(expected, response.getBody());
    }
}
