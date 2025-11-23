package com.moveit.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour retourner une liste de notifications (scroll infini).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationListResponse {

    private List<NotificationResponse> notifications;
    private long unreadCount; // Nombre de notifications non lues pour le badge
    private boolean hasMore; // Y a-t-il plus de notifications à charger ?
    private Long lastNotificationId; // ID de la dernière notification (pour le curseur)
}
