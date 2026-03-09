package com.moveit.notification.service.impl;

import com.moveit.notification.dto.NotificationResponseDTO;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.mapper.NotificationMapper;
import com.moveit.notification.repository.NotificationRepository;
import com.moveit.notification.repository.SubscriptionRepository;
import com.moveit.notification.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implémentation du service de gestion des connexions SSE (Server-Sent Events).
 *
 * - Chaque userId peut avoir plusieurs emitters (plusieurs onglets/devices).
 * - Gestion du Last-Event-ID : à la reconnexion, le back renvoie les notifs manquées.
 * - Chaque event SSE est identifié par l'ID de la notification pour que le client
 *   puisse le renvoyer dans le header Last-Event-ID à la reconnexion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SseEmitterServiceImpl implements SseEmitterService {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutes

    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationMapper notificationMapper;

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(String userId, Long lastEventId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connection established for user " + userId));

            if (lastEventId != null) {
                replayMissedNotifications(userId, lastEventId, emitter);
            }
        } catch (IOException e) {
            log.warn("Failed to send connection event to user {}", userId);
            removeEmitter(userId, emitter);
        }

        log.debug("User {} subscribed to SSE (lastEventId={})", userId, lastEventId);
        return emitter;
    }

    @Override
    public void sendToUser(String userId, NotificationResponseDTO dto) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            log.trace("No active SSE connection for user {}", userId);
            return;
        }

        List<SseEmitter> deadEmitters = new java.util.ArrayList<>();

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(dto.getId()))
                        .name("notification")
                        .data(dto));
            } catch (IOException e) {
                log.trace("Failed to send SSE to user {}, removing emitter", userId);
                deadEmitters.add(emitter);
            }
        }

        userEmitters.removeAll(deadEmitters);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }

    @Override
    public void broadcastToSubscribers(Notification notification) {
        List<String> activeUserIds = subscriptionRepository
                .findActiveUserIdsByNotificationType(notification.getNotificationType());

        if (activeUserIds.isEmpty()) {
            log.debug("No active subscribers for notification type {}", notification.getNotificationType());
            return;
        }

        log.debug("Broadcasting notification {} to {} users via SSE", notification.getId(), activeUserIds.size());

        NotificationResponseDTO dto = notificationMapper.toResponseDTO(notification);

        for (String userId : activeUserIds) {
            sendToUser(userId, dto);
        }
    }

    private void replayMissedNotifications(String userId, Long lastEventId, SseEmitter emitter) {
        var activeTypes = subscriptionRepository.findActiveNotificationTypesByUserId(userId);
        if (activeTypes.isEmpty()) return;

        List<NotificationResponseDTO> missed = notificationRepository
                .findByIdGreaterThanOrderByIdAsc(lastEventId)
                .stream()
                .filter(n -> activeTypes.contains(n.getNotificationType()))
                .map(notificationMapper::toResponseDTO)
                .toList();

        log.debug("Replaying {} missed notifications to user {}", missed.size(), userId);

        for (NotificationResponseDTO dto : missed) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(dto.getId()))
                        .name("notification")
                        .data(dto));
            } catch (IOException e) {
                log.warn("Failed to replay notification {} to user {}", dto.getId(), userId);
                break;
            }
        }
    }

    private void removeEmitter(String userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
        log.trace("Emitter removed for user {}", userId);
    }
}

