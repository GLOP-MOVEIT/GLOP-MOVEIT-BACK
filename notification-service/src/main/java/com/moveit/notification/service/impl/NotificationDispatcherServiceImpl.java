package com.moveit.notification.service.impl;

import com.moveit.notification.entity.Notification;
import com.moveit.notification.repository.SubscriptionRepository;
import com.moveit.notification.service.NotificationDispatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service de dispatch des notifications via WebSocket.
 * 
 * P0 - Sécurité : Utilise convertAndSendToUser() pour isoler les queues par utilisateur.
 *      Chaque user ne reçoit que SES notifications, pas celles des autres.
 * 
 * P1 - Performance : Utilise une query optimisée qui retourne directement les userIds
 *      au lieu de charger toutes les entités Subscription en mémoire.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcherServiceImpl implements NotificationDispatcherService {

    private final SubscriptionRepository subscriptionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Async
    public void dispatch(Notification notification) {
        // P1 - Query optimisée : récupère directement les userIds actifs (pas les entités complètes)
        List<String> activeUserIds = subscriptionRepository
                .findActiveUserIdsByNotificationType(notification.getNotificationType());

        log.debug("Dispatching notification {} to {} users", notification.getId(), activeUserIds.size());

        // P0 - Isolation : envoie à chaque user sur sa queue privée
        for (String userId : activeUserIds) {
            sendToUser(userId, notification);
        }
    }

    /**
     * P0 - Envoie la notification sur la queue privée de l'utilisateur.
     * Utilise convertAndSendToUser() qui route vers /user/{userId}/queue/notifications
     * 
     * Le client doit s'abonner à : /user/queue/notifications
     * Spring ajoute automatiquement le préfixe /user/{sessionId} ou /user/{userId}
     */
    private void sendToUser(String userId, Notification notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/notifications",
                    notification
            );
            log.trace("Notification {} sent to user {}", notification.getId(), userId);
        } catch (Exception e) {
            log.warn("Failed to send notification {} to user {}: {}", 
                    notification.getId(), userId, e.getMessage());
        }
    }
}
