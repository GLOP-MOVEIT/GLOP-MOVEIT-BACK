package com.moveit.notification.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.notification.dto.NotificationCreateDTO;
import com.moveit.notification.dto.NotificationUpdateDTO;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Point 8 - Integration tests with real database (H2)
 * Tests the full application flow end-to-end without mocks
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("NotificationIntegration ")
class NotificationIntegrationTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        notificationRepository.deleteAll();
    }

    private Notification createNotification(String title, String content, NotificationType type) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotificationType(type);
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    private NotificationCreateDTO createDTO(String title, String content, NotificationType type) {
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setTitle(title);
        dto.setContent(content);
        dto.setNotificationType(type);
        return dto;
    }

    @Test
    @DisplayName("Create notification and retrieve it end-to-end")
    void testCreateAndRetrieveNotification() throws Exception {
        NotificationCreateDTO createDTO = createDTO("Integration Test", "Content", NotificationType.INCIDENT);

        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())  // Le controlleur retourne 200
                .andExpect(jsonPath("$.title").value("Integration Test"))
                .andExpect(jsonPath("$.notificationType").value("INCIDENT"));

        assertThat(notificationRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Retrieve all notifications with pagination")
    void testGetAllNotificationsWithPagination() throws Exception {
        for (int i = 0; i < 15; i++) {
            createNotification("Notification " + i, "Content " + i, NotificationType.EVENT);
        }

        mockMvc.perform(get("/notifications")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @DisplayName("Sort notifications by creation date")
    void testSortNotificationsByCreatedAt() throws Exception {
        Notification first = new Notification();
        first.setTitle("First");
        first.setContent("Content 1");
        first.setNotificationType(NotificationType.SYSTEM);
        first.setCreatedAt(LocalDateTime.of(2020, 1, 1, 10, 0));
        notificationRepository.save(first);

        Notification second = new Notification();
        second.setTitle("Second");
        second.setContent("Content 2");
        second.setNotificationType(NotificationType.ALERT);
        second.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        notificationRepository.save(second);

        mockMvc.perform(get("/notifications")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Validation error on empty title")
    void testCreateNotificationWithoutTitle() throws Exception {
        NotificationCreateDTO createDTO = createDTO("", "Valid content", NotificationType.INCIDENT);

        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get non-existent notification returns 404")
    void testGetNonExistentNotification() throws Exception {
        mockMvc.perform(get("/notifications/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update notification with new content")
    void testUpdateNotificationContent() throws Exception {
        Notification saved = createNotification("Original", "Original Content", NotificationType.INCIDENT);

        NotificationUpdateDTO updateDTO = new NotificationUpdateDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setContent("Updated Content");

        mockMvc.perform(put("/notifications/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"));

        Notification updated = notificationRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("Delete notification removes it from database")
    void testDeleteNotification() throws Exception {
        Notification saved = createNotification("To Delete", "Delete me", NotificationType.SYSTEM);

        mockMvc.perform(delete("/notifications/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(notificationRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    @DisplayName("Filter by notification type")
    void testGetNotificationsByType() throws Exception {
        createNotification("Info Notification", "Info content", NotificationType.EVENT);
        createNotification("Warning Notification", "Warning content", NotificationType.ALERT);

        mockMvc.perform(get("/notifications")
                        .param("type", "EVENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].notificationType").value("EVENT"));
    }

    @Test
    @DisplayName("Invalid pagination parameters return 400")
    void testInvalidPaginationParameters() throws Exception {
        mockMvc.perform(get("/notifications")
                        .param("page", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Invalid sort field returns 400")
    void testInvalidSortField() throws Exception {
        mockMvc.perform(get("/notifications")
                        .param("sortBy", "invalidField"))
                .andExpect(status().isBadRequest());
    }
}
