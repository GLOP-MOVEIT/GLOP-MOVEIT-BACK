package com.moveit.notification.controller;

import com.moveit.notification.dto.MarkAsReadRequest;
import com.moveit.notification.dto.NotificationListResponse;
import com.moveit.notification.dto.NotificationRequest;
import com.moveit.notification.dto.NotificationResponse;
import com.moveit.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Crée une nouvelle notification.
     *
     * POST /api/notifications
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("POST /api/notifications - Creating notification for user {}", request.getUserId());
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupère les notifications d'un utilisateur avec scroll infini.
     *
     * GET /api/notifications/user/{userId}?levelName=CRITIQUE&read=false&limit=20&afterId=123
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<NotificationListResponse> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(required = false) String levelName,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Long afterId
    ) {
        log.info("GET /api/notifications/user/{} - Fetching notifications (levelName={}, read={}, limit={}, afterId={})",
                userId, levelName, read, limit, afterId);

        NotificationListResponse response = notificationService.getUserNotifications(
                userId, levelName, read, limit, afterId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les notifications critiques non lues d'un utilisateur.
     *
     * GET /api/notifications/user/{userId}/critical-alerts
     */
    @GetMapping("/user/{userId}/critical-alerts")
    public ResponseEntity<List<NotificationResponse>> getCriticalAlerts(@PathVariable Long userId) {
        log.info("GET /api/notifications/user/{}/critical-alerts - Fetching critical alerts", userId);
        List<NotificationResponse> alerts = notificationService.getCriticalAlerts(userId);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Marque une notification comme lue ou non lue.
     *
     * PATCH /api/notifications/{id}/read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @Valid @RequestBody MarkAsReadRequest request
    ) {
        log.info("PATCH /api/notifications/{}/read - Marking as read={}", id, request.getRead());
        NotificationResponse response = notificationService.markAsRead(id, request.getRead());
        return ResponseEntity.ok(response);
    }

    /**
     * Marque toutes les notifications d'un utilisateur comme lues.
     *
     * POST /api/notifications/user/{userId}/mark-all-read
     */
    @PostMapping("/user/{userId}/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        log.info("POST /api/notifications/user/{}/mark-all-read - Marking all as read", userId);
        int count = notificationService.markAllAsRead(userId);
        log.info("{} notifications marked as read for user {}", count, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Compte les notifications non lues d'un utilisateur (pour le badge).
     *
     * GET /api/notifications/user/{userId}/unread-count
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        log.debug("GET /api/notifications/user/{}/unread-count", userId);
        long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(count);
    }
}
