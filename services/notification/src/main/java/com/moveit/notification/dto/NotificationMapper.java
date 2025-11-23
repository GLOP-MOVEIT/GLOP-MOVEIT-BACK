package com.moveit.notification.dto;

import com.moveit.notification.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper pour convertir entre entités et DTOs.
 */
@Component
public class NotificationMapper {

    /**
     * Convertit une entité Notification en NotificationResponse.
     */
    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .typeName(notification.getType().name())
                .typeDescription(notification.getType().getDescriptionKey())
                .levelName(notification.getLevel().getName())
                .levelPriority(notification.getLevel().getPriority())
                .name(notification.getName())
                .body(notification.getBody())
                .securityId(notification.getSecurityId())
                .competitionId(notification.getCompetitionId())
                .read(notification.getRead())
                .createdAt(notification.getStartDate())
                .mandatory(notification.getType().isMandatory())
                .build();
    }

    /**
     * Convertit une liste d'entités en NotificationListResponse.
     */
    public NotificationListResponse toListResponse(List<Notification> notifications, long unreadCount, boolean hasMore) {
        return NotificationListResponse.builder()
                .notifications(notifications.stream()
                        .map(this::toResponse)
                        .toList())
                .unreadCount(unreadCount)
                .hasMore(hasMore)
                .lastNotificationId(notifications.isEmpty() ? null : notifications.get(notifications.size() - 1).getId())
                .build();
    }
}
