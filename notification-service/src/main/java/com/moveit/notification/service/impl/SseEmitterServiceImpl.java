package com.moveit.notification.service.impl;

import com.moveit.notification.dto.NotificationResponseDTO;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.Subscription;
import com.moveit.notification.entity.TargetType;
import com.moveit.notification.mapper.NotificationMapper;
import com.moveit.notification.repository.NotificationRepository;
import com.moveit.notification.repository.SubscriptionRepository;
import com.moveit.notification.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
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
        } catch (IOException _) {
            removeEmitter(userId, emitter);
        }

        return emitter;
    }

    @Override
    public void sendToUser(String userId, NotificationResponseDTO dto) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new java.util.ArrayList<>();

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(dto.getId()))
                        .name("notification")
                        .data(dto));
            } catch (IOException _) {
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
        List<String> activeUserIds;

        if (notification.getTargetType() == TargetType.GLOBAL) {
            // Notif globale : envoyer aux abonnés GLOBAL de ce type
            activeUserIds = subscriptionRepository.findActiveUserIdsForGlobal(notification.getNotificationType());
        } else {
            // Notif ciblée : envoyer aux abonnés de ce target + aux abonnés GLOBAL du même type
            activeUserIds = subscriptionRepository.findActiveUserIdsForTarget(
                    notification.getNotificationType(),
                    notification.getTargetType(),
                    notification.getTargetId());
        }

        if (activeUserIds.isEmpty()) {
            return;
        }

        NotificationResponseDTO dto = notificationMapper.toResponseDTO(notification);

        for (String userId : activeUserIds) {
            sendToUser(userId, dto);
        }
    }

    private void replayMissedNotifications(String userId, Long lastEventId, SseEmitter emitter) {
        List<Subscription> activeSubscriptions = subscriptionRepository.findActiveSubscriptionsByUserId(userId);
        if (activeSubscriptions.isEmpty()) return;

        List<NotificationResponseDTO> missed = notificationRepository
                .findByIdGreaterThanOrderByIdAsc(lastEventId)
                .stream()
                .filter(n -> matchesAnySubscription(n, activeSubscriptions))
                .map(notificationMapper::toResponseDTO)
                .toList();

        for (NotificationResponseDTO dto : missed) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(dto.getId()))
                        .name("notification")
                        .data(dto));
            } catch (IOException _) {
                break;
            }
        }
    }

    /**
     * Vérifie si une notification correspond à au moins une subscription.
     * Un abonnement GLOBAL matche toutes les notifs du même type.
     * Un abonnement ciblé matche seulement si targetType + targetId correspondent.
     */
    private boolean matchesAnySubscription(Notification notification, List<Subscription> subscriptions) {
        return subscriptions.stream().anyMatch(sub -> {
            if (sub.getNotificationType() != notification.getNotificationType()) {
                return false;
            }
            if (sub.getTargetType() == TargetType.GLOBAL) {
                return true;
            }
            return sub.getTargetType() == notification.getTargetType()
                    && sub.getTargetId() != null
                    && sub.getTargetId().equals(notification.getTargetId());
        });
    }

    private void removeEmitter(String userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }
}

