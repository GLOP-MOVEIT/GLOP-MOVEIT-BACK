package com.moveit.notification.mapper;

import com.moveit.notification.dto.NotificationResponseDTO;
import com.moveit.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponseDTO toResponseDTO(Notification notification) {
        if (notification == null) {
            return null;
        }
        
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setNotificationType(notification.getNotificationType());
        dto.setIncidentIds(notification.getIncidentIds());
        dto.setEventIds(notification.getEventIds());
        dto.setCreatedAt(notification.getCreatedAt());
        
        return dto;
    }
}
