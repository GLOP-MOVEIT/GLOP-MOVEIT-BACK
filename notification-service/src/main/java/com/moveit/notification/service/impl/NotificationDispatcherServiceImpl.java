package com.moveit.notification.service.impl;

import com.moveit.notification.dto.NotificationResponseDTO;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.mapper.NotificationMapper;
import com.moveit.notification.repository.SubscriptionRepository;
import com.moveit.notification.service.NotificationDispatcherService;
import com.moveit.notification.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service de dispatch des notifications via SSE (Server-Sent Events).
 *
 * Remplace WebSocket/STOMP par du SSE natif HTTP, plus léger et sans dépendance externe.
 * Chaque utilisateur abonné à un type de notification reçoit un event SSE en temps réel.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcherServiceImpl implements NotificationDispatcherService {

    private final SubscriptionRepository subscriptionRepository;
    private final SseEmitterService sseEmitterService;
    private final NotificationMapper notificationMapper;

    @Override
    @Async
    public void dispatch(Notification notification) {
        List<String> activeUserIds = subscriptionRepository
                .findActiveUserIdsByNotificationType(notification.getNotificationType());

        log.debug("Dispatching notification {} to {} users via SSE", notification.getId(), activeUserIds.size());

        NotificationResponseDTO dto = notificationMapper.toResponseDTO(notification);

        for (String userId : activeUserIds) {
            sseEmitterService.sendToUser(userId, dto);
        }
    }
}
