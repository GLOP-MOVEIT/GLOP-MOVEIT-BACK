package com.moveit.notification.service.impl;

import com.moveit.notification.dto.NotificationListResponse;
import com.moveit.notification.dto.NotificationMapper;
import com.moveit.notification.dto.NotificationRequest;
import com.moveit.notification.dto.NotificationResponse;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationLevel;
import com.moveit.notification.repository.NotificationLevelRepository;
import com.moveit.notification.repository.NotificationRepository;
import com.moveit.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implémentation du service de gestion des notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationLevelRepository notificationLevelRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("Creating notification for user {} with type {}", request.getUserId(), request.getType());

        // Récupération du niveau de notification
        NotificationLevel level = notificationLevelRepository.findByName(request.getLevelName())
                .orElseThrow(() -> new IllegalArgumentException("Invalid level name: " + request.getLevelName()));

        // Création de l'entité
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setType(request.getType());
        notification.setLevel(level);
        notification.setName(request.getName());
        notification.setBody(request.getBody());
        notification.setSecurityId(request.getSecurityId());
        notification.setCompetitionId(request.getCompetitionId());
        notification.setRead(false);

        // Sauvegarde en base
        Notification savedNotification = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toResponse(savedNotification);

        // Envoi en temps réel via WebSocket
        sendNotificationViaWebSocket(response);

        log.info("Notification {} created and sent to user {}", savedNotification.getId(), request.getUserId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationListResponse getUserNotifications(Long userId, String levelName, Boolean read, int limit, Long afterId) {
        log.debug("Fetching notifications for user {} (levelName={}, read={}, limit={}, afterId={})",
                userId, levelName, read, limit, afterId);

        // Récupérer limit + 1 pour détecter s'il y a plus de résultats
        Pageable pageable = PageRequest.of(0, limit + 1);

        // Récupération des notifications avec filtres
        List<Notification> notifications = notificationRepository.findByUserIdWithFilters(
                userId, levelName, read, pageable
        ).getContent();

        // Filtrage par curseur (afterId) pour scroll infini
        if (afterId != null) {
            notifications = notifications.stream()
                    .filter(n -> n.getId() < afterId)
                    .toList();
        }

        // Vérifier s'il y a plus de notifications à charger
        boolean hasMore = notifications.size() > limit;

        // Tronquer à la limite demandée
        if (hasMore) {
            notifications = notifications.subList(0, limit);
        }

        // Compter les non lues pour le badge
        long unreadCount = notificationRepository.countByUserIdAndReadFalse(userId);

        return notificationMapper.toListResponse(notifications, unreadCount, hasMore);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getCriticalAlerts(Long userId) {
        log.debug("Fetching critical alerts for user {}", userId);

        List<Notification> criticalNotifications = notificationRepository.findCriticalUnreadByUserId(userId);

        return criticalNotifications.stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Boolean read) {
        log.info("Marking notification {} as read={}", notificationId, read);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        notification.setRead(read);
        Notification updatedNotification = notificationRepository.save(notification);

        return notificationMapper.toResponse(updatedNotification);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user {}", userId);

        List<Notification> unreadNotifications = notificationRepository.findByUserIdWithFilters(
                userId, null, false, Pageable.unpaged()
        ).getContent();

        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);

        log.info("{} notifications marked as read for user {}", unreadNotifications.size(), userId);
        return unreadNotifications.size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    /**
     * Envoie une notification en temps réel via WebSocket.
     *
     * @param notification La notification à envoyer
     */
    private void sendNotificationViaWebSocket(NotificationResponse notification) {
        try {
            // Envoi à l'utilisateur spécifique via WebSocket
            // Destination: /user/{userId}/queue/notifications
            messagingTemplate.convertAndSendToUser(
                    notification.getUserId().toString(),
                    "/queue/notifications",
                    notification
            );
            log.debug("Notification sent via WebSocket to user {}", notification.getUserId());
        } catch (Exception e) {
            log.error("Failed to send notification via WebSocket to user {}: {}",
                    notification.getUserId(), e.getMessage());
            // Ne pas faire échouer la création de notification si WebSocket échoue
        }
    }
}
