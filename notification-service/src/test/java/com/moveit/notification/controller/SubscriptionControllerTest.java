package com.moveit.notification.controller;

import com.moveit.notification.dto.SubscriptionCreateDTO;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.Subscription;
import com.moveit.notification.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionController Unit Tests")
class SubscriptionControllerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionController subscriptionController;

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
    @DisplayName("getSubscriptions should return all subscriptions")
    void testGetSubscriptions() {
        List<Subscription> subscriptions = Arrays.asList(subscription);
        when(subscriptionService.getSubscriptions(null, null)).thenReturn(subscriptions);

        ResponseEntity<List<Subscription>> response = subscriptionController.getSubscriptions(null, null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("user123", response.getBody().get(0).getUserId());
        verify(subscriptionService, times(1)).getSubscriptions(null, null);
    }

    @Test
    @DisplayName("getSubscriptions with userId filter should return filtered subscriptions")
    void testGetSubscriptionsWithUserId() {
        List<Subscription> subscriptions = Arrays.asList(subscription);
        when(subscriptionService.getSubscriptions("user123", null)).thenReturn(subscriptions);

        ResponseEntity<List<Subscription>> response = subscriptionController.getSubscriptions("user123", null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("user123", response.getBody().get(0).getUserId());
        verify(subscriptionService, times(1)).getSubscriptions("user123", null);
    }

    @Test
    @DisplayName("getSubscriptions with type filter should return filtered subscriptions")
    void testGetSubscriptionsWithType() {
        List<Subscription> subscriptions = Arrays.asList(subscription);
        when(subscriptionService.getSubscriptions(null, NotificationType.INCIDENT)).thenReturn(subscriptions);

        ResponseEntity<List<Subscription>> response = subscriptionController.getSubscriptions(null, NotificationType.INCIDENT);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(NotificationType.INCIDENT, response.getBody().get(0).getNotificationType());
        verify(subscriptionService, times(1)).getSubscriptions(null, NotificationType.INCIDENT);
    }

    @Test
    @DisplayName("createSubscription should create and return subscription")
    void testCreateSubscription() {
        when(subscriptionService.createSubscription(any(SubscriptionCreateDTO.class))).thenReturn(subscription);

        ResponseEntity<Subscription> response = subscriptionController.createSubscription(createDTO);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("user123", response.getBody().getUserId());
        verify(subscriptionService, times(1)).createSubscription(any(SubscriptionCreateDTO.class));
    }

    @Test
    @DisplayName("toggleSubscription should toggle subscription when found")
    void testToggleSubscription_Found() {
        subscription.setActive(false);
        when(subscriptionService.toggleSubscription(1L)).thenReturn(Optional.of(subscription));

        ResponseEntity<Subscription> response = subscriptionController.toggleSubscription(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getActive());
        verify(subscriptionService, times(1)).toggleSubscription(1L);
    }

    @Test
    @DisplayName("toggleSubscription should return 404 when not found")
    void testToggleSubscription_NotFound() {
        when(subscriptionService.toggleSubscription(999L)).thenReturn(Optional.empty());

        ResponseEntity<Subscription> response = subscriptionController.toggleSubscription(999L);

        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        verify(subscriptionService, times(1)).toggleSubscription(999L);
    }

    @Test
    @DisplayName("deleteSubscription should delete subscription")
    void testDeleteSubscription() {
        doNothing().when(subscriptionService).deleteSubscription(1L);

        ResponseEntity<Void> response = subscriptionController.deleteSubscription(1L);

        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value());
        verify(subscriptionService, times(1)).deleteSubscription(1L);
    }
}
