package com.moveit.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.notification.dto.SubscriptionCreateDTO;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.Subscription;
import com.moveit.notification.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SubscriptionController.class)
@Import(ObjectMapper.class)
@DisplayName("SubscriptionController WebMvc Tests")
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SubscriptionService subscriptionService;

    private Subscription subscription;
    private SubscriptionCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        subscription = new Subscription();
        subscription.setId(1L);
        subscription.setUserId("user123");
        subscription.setNotificationType(NotificationType.INCIDENT);
        subscription.setActive(true);

        createDTO = new SubscriptionCreateDTO();
        createDTO.setUserId("user123");
        createDTO.setNotificationType(NotificationType.EVENT);
    }

    @Test
    @DisplayName("GET /subscriptions should return all subscriptions")
    void testGetSubscriptions() throws Exception {
        List<Subscription> subscriptions = Arrays.asList(subscription);
        when(subscriptionService.getSubscriptions(null, null)).thenReturn(subscriptions);

        mockMvc.perform(get("/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value("user123"))
                .andExpect(jsonPath("$[0].notificationType").value("INCIDENT"));

        verify(subscriptionService, times(1)).getSubscriptions(null, null);
    }

    @Test
    @DisplayName("GET /subscriptions with userId filter should return filtered subscriptions")
    void testGetSubscriptionsWithUserId() throws Exception {
        List<Subscription> subscriptions = Arrays.asList(subscription);
        when(subscriptionService.getSubscriptions("user123", null)).thenReturn(subscriptions);

        mockMvc.perform(get("/subscriptions")
                        .param("userId", "user123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user123"));

        verify(subscriptionService, times(1)).getSubscriptions("user123", null);
    }

    @Test
    @DisplayName("GET /subscriptions with type filter should return filtered subscriptions")
    void testGetSubscriptionsWithType() throws Exception {
        List<Subscription> subscriptions = Arrays.asList(subscription);
        when(subscriptionService.getSubscriptions(null, NotificationType.INCIDENT)).thenReturn(subscriptions);

        mockMvc.perform(get("/subscriptions")
                        .param("type", "INCIDENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationType").value("INCIDENT"));

        verify(subscriptionService, times(1)).getSubscriptions(null, NotificationType.INCIDENT);
    }

    @Test
    @DisplayName("POST /subscriptions should create subscription")
    void testCreateSubscription() throws Exception {
        when(subscriptionService.createSubscription(any(SubscriptionCreateDTO.class))).thenReturn(subscription);

        mockMvc.perform(post("/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.notificationType").value("INCIDENT"));

        verify(subscriptionService, times(1)).createSubscription(any(SubscriptionCreateDTO.class));
    }

    @Test
    @DisplayName("PATCH /subscriptions/{id}/toggle should toggle subscription when found")
    void testToggleSubscription_Found() throws Exception {
        subscription.setActive(false);
        when(subscriptionService.toggleSubscription(1L)).thenReturn(Optional.of(subscription));

        mockMvc.perform(patch("/subscriptions/1/toggle")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(subscriptionService, times(1)).toggleSubscription(1L);
    }

    @Test
    @DisplayName("PATCH /subscriptions/{id}/toggle should return 404 when not found")
    void testToggleSubscription_NotFound() throws Exception {
        when(subscriptionService.toggleSubscription(999L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/subscriptions/999/toggle")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(subscriptionService, times(1)).toggleSubscription(999L);
    }

    @Test
    @DisplayName("DELETE /subscriptions/{id} should delete subscription")
    void testDeleteSubscription() throws Exception {
        doNothing().when(subscriptionService).deleteSubscription(1L);

        mockMvc.perform(delete("/subscriptions/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(subscriptionService, times(1)).deleteSubscription(1L);
    }
}
