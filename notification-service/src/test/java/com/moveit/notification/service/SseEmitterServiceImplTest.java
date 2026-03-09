package com.moveit.notification.service;

import com.moveit.notification.dto.NotificationResponseDTO;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.Subscription;
import com.moveit.notification.entity.TargetType;
import com.moveit.notification.mapper.NotificationMapper;
import com.moveit.notification.repository.NotificationRepository;
import com.moveit.notification.repository.SubscriptionRepository;
import com.moveit.notification.service.impl.SseEmitterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("SseEmitterServiceImpl Tests")
class SseEmitterServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private SseEmitterServiceImpl sseEmitterService;

    private Notification globalNotification;
    private Notification targetedNotification;
    private NotificationResponseDTO globalDto;
    private NotificationResponseDTO targetedDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        globalNotification = new Notification();
        globalNotification.setId(1L);
        globalNotification.setTitle("Global Alert");
        globalNotification.setContent("Content");
        globalNotification.setNotificationType(NotificationType.ALERT);
        globalNotification.setTargetType(TargetType.GLOBAL);
        globalNotification.setTargetId(null);
        globalNotification.setCreatedAt(LocalDateTime.now());

        targetedNotification = new Notification();
        targetedNotification.setId(2L);
        targetedNotification.setTitle("Competition Result");
        targetedNotification.setContent("Content");
        targetedNotification.setNotificationType(NotificationType.RESULT);
        targetedNotification.setTargetType(TargetType.COMPETITION);
        targetedNotification.setTargetId(42L);
        targetedNotification.setCreatedAt(LocalDateTime.now());

        globalDto = new NotificationResponseDTO();
        globalDto.setId(1L);
        globalDto.setTitle("Global Alert");
        globalDto.setNotificationType(NotificationType.ALERT);
        globalDto.setTargetType(TargetType.GLOBAL);

        targetedDto = new NotificationResponseDTO();
        targetedDto.setId(2L);
        targetedDto.setTitle("Competition Result");
        targetedDto.setNotificationType(NotificationType.RESULT);
        targetedDto.setTargetType(TargetType.COMPETITION);
        targetedDto.setTargetId(42L);
    }

    // ==================== subscribe ====================

    @Test
    @DisplayName("subscribe should return a non-null SseEmitter")
    void subscribe_shouldReturnEmitter() {
        SseEmitter emitter = sseEmitterService.subscribe("user1", null);

        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("subscribe with lastEventId should replay missed notifications")
    void subscribe_withLastEventId_shouldReplay() {
        Subscription sub = buildSubscription("user1", NotificationType.ALERT, TargetType.GLOBAL, null);
        when(subscriptionRepository.findActiveSubscriptionsByUserId("user1")).thenReturn(List.of(sub));
        when(notificationRepository.findByIdGreaterThanOrderByIdAsc(5L)).thenReturn(List.of(globalNotification));
        when(notificationMapper.toResponseDTO(globalNotification)).thenReturn(globalDto);

        SseEmitter emitter = sseEmitterService.subscribe("user1", 5L);

        assertThat(emitter).isNotNull();
        verify(notificationRepository).findByIdGreaterThanOrderByIdAsc(5L);
        verify(subscriptionRepository).findActiveSubscriptionsByUserId("user1");
    }

    @Test
    @DisplayName("subscribe without lastEventId should NOT replay")
    void subscribe_withoutLastEventId_shouldNotReplay() {
        SseEmitter emitter = sseEmitterService.subscribe("user1", null);

        assertThat(emitter).isNotNull();
        verify(notificationRepository, never()).findByIdGreaterThanOrderByIdAsc(anyLong());
    }

    // ==================== broadcastToSubscribers ====================

    @Test
    @DisplayName("broadcastToSubscribers with GLOBAL notification should query global subscribers")
    void broadcastToSubscribers_global_shouldQueryGlobal() {
        when(subscriptionRepository.findActiveUserIdsForGlobal(NotificationType.ALERT))
                .thenReturn(List.of("user1", "user2"));
        when(notificationMapper.toResponseDTO(globalNotification)).thenReturn(globalDto);

        // Subscribe users first so they have emitters
        sseEmitterService.subscribe("user1", null);
        sseEmitterService.subscribe("user2", null);

        sseEmitterService.broadcastToSubscribers(globalNotification);

        verify(subscriptionRepository).findActiveUserIdsForGlobal(NotificationType.ALERT);
        verify(subscriptionRepository, never()).findActiveUserIdsForTarget(any(), any(), anyLong());
        verify(notificationMapper).toResponseDTO(globalNotification);
    }

    @Test
    @DisplayName("broadcastToSubscribers with targeted notification should query target subscribers")
    void broadcastToSubscribers_targeted_shouldQueryTarget() {
        when(subscriptionRepository.findActiveUserIdsForTarget(NotificationType.RESULT, TargetType.COMPETITION, 42L))
                .thenReturn(List.of("user1"));
        when(notificationMapper.toResponseDTO(targetedNotification)).thenReturn(targetedDto);

        sseEmitterService.subscribe("user1", null);

        sseEmitterService.broadcastToSubscribers(targetedNotification);

        verify(subscriptionRepository).findActiveUserIdsForTarget(NotificationType.RESULT, TargetType.COMPETITION, 42L);
        verify(subscriptionRepository, never()).findActiveUserIdsForGlobal(any());
        verify(notificationMapper).toResponseDTO(targetedNotification);
    }

    @Test
    @DisplayName("broadcastToSubscribers with no subscribers should not send anything")
    void broadcastToSubscribers_noSubscribers_shouldNotSend() {
        when(subscriptionRepository.findActiveUserIdsForGlobal(NotificationType.ALERT))
                .thenReturn(List.of());

        sseEmitterService.broadcastToSubscribers(globalNotification);

        verify(subscriptionRepository).findActiveUserIdsForGlobal(NotificationType.ALERT);
        verify(notificationMapper, never()).toResponseDTO(any());
    }

    // ==================== sendToUser ====================

    @Test
    @DisplayName("sendToUser should not fail when user has no active emitters")
    void sendToUser_noEmitters_shouldNotFail() {
        sseEmitterService.sendToUser("unknownUser", globalDto);
        // No exception thrown = success
    }

    @Test
    @DisplayName("sendToUser should send to user with active emitter")
    void sendToUser_withEmitter_shouldSend() {
        sseEmitterService.subscribe("user1", null);

        // Should not throw
        sseEmitterService.sendToUser("user1", globalDto);
    }

    // ==================== replay filtering by subscription target ====================

    @Test
    @DisplayName("replay should NOT include notifications that don't match subscription target")
    void subscribe_replay_shouldFilterByTarget() {
        // User subscribed to RESULT for COMPETITION 42 only
        Subscription sub = buildSubscription("user1", NotificationType.RESULT, TargetType.COMPETITION, 42L);
        when(subscriptionRepository.findActiveSubscriptionsByUserId("user1")).thenReturn(List.of(sub));

        // Missed notifications: one for competition 42 (match), one for competition 99 (no match)
        Notification matchNotif = new Notification();
        matchNotif.setId(10L);
        matchNotif.setNotificationType(NotificationType.RESULT);
        matchNotif.setTargetType(TargetType.COMPETITION);
        matchNotif.setTargetId(42L);

        Notification noMatchNotif = new Notification();
        noMatchNotif.setId(11L);
        noMatchNotif.setNotificationType(NotificationType.RESULT);
        noMatchNotif.setTargetType(TargetType.COMPETITION);
        noMatchNotif.setTargetId(99L);

        when(notificationRepository.findByIdGreaterThanOrderByIdAsc(5L))
                .thenReturn(List.of(matchNotif, noMatchNotif));

        NotificationResponseDTO matchDto = new NotificationResponseDTO();
        matchDto.setId(10L);
        when(notificationMapper.toResponseDTO(matchNotif)).thenReturn(matchDto);

        sseEmitterService.subscribe("user1", 5L);

        // Only the matching notification should be mapped
        verify(notificationMapper).toResponseDTO(matchNotif);
        verify(notificationMapper, never()).toResponseDTO(noMatchNotif);
    }

    @Test
    @DisplayName("replay should include ALL notifications of same type for GLOBAL subscriber")
    void subscribe_replay_globalSubscriber_shouldIncludeAll() {
        // User subscribed GLOBAL to ALERT
        Subscription sub = buildSubscription("user1", NotificationType.ALERT, TargetType.GLOBAL, null);
        when(subscriptionRepository.findActiveSubscriptionsByUserId("user1")).thenReturn(List.of(sub));

        Notification globalAlert = new Notification();
        globalAlert.setId(10L);
        globalAlert.setNotificationType(NotificationType.ALERT);
        globalAlert.setTargetType(TargetType.GLOBAL);

        Notification compAlert = new Notification();
        compAlert.setId(11L);
        compAlert.setNotificationType(NotificationType.ALERT);
        compAlert.setTargetType(TargetType.COMPETITION);
        compAlert.setTargetId(42L);

        when(notificationRepository.findByIdGreaterThanOrderByIdAsc(5L))
                .thenReturn(List.of(globalAlert, compAlert));

        NotificationResponseDTO dto1 = new NotificationResponseDTO();
        dto1.setId(10L);
        NotificationResponseDTO dto2 = new NotificationResponseDTO();
        dto2.setId(11L);
        when(notificationMapper.toResponseDTO(globalAlert)).thenReturn(dto1);
        when(notificationMapper.toResponseDTO(compAlert)).thenReturn(dto2);

        sseEmitterService.subscribe("user1", 5L);

        // Global subscriber should receive both ALERT notifications
        verify(notificationMapper).toResponseDTO(globalAlert);
        verify(notificationMapper).toResponseDTO(compAlert);
    }

    @Test
    @DisplayName("replay should skip notifications of different type")
    void subscribe_replay_shouldSkipDifferentType() {
        Subscription sub = buildSubscription("user1", NotificationType.ALERT, TargetType.GLOBAL, null);
        when(subscriptionRepository.findActiveSubscriptionsByUserId("user1")).thenReturn(List.of(sub));

        Notification resultNotif = new Notification();
        resultNotif.setId(10L);
        resultNotif.setNotificationType(NotificationType.RESULT);
        resultNotif.setTargetType(TargetType.GLOBAL);

        when(notificationRepository.findByIdGreaterThanOrderByIdAsc(5L))
                .thenReturn(List.of(resultNotif));

        sseEmitterService.subscribe("user1", 5L);

        // RESULT notification should NOT be mapped for ALERT subscriber
        verify(notificationMapper, never()).toResponseDTO(resultNotif);
    }

    @Test
    @DisplayName("replay with no active subscriptions should not replay anything")
    void subscribe_replay_noSubscriptions_shouldNotReplay() {
        when(subscriptionRepository.findActiveSubscriptionsByUserId("user1")).thenReturn(List.of());

        sseEmitterService.subscribe("user1", 5L);

        verify(notificationRepository, never()).findByIdGreaterThanOrderByIdAsc(anyLong());
    }

    // ==================== helpers ====================

    private Subscription buildSubscription(String userId, NotificationType type, TargetType targetType, Long targetId) {
        Subscription sub = new Subscription();
        sub.setId(1L);
        sub.setUserId(userId);
        sub.setNotificationType(type);
        sub.setTargetType(targetType);
        sub.setTargetId(targetId);
        sub.setActive(true);
        return sub;
    }
}

