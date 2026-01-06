package com.moveit.notification.service;

import com.moveit.notification.dto.NotificationCreateDTO;
import com.moveit.notification.dto.NotificationUpdateDTO;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationType;

import java.util.List;
import java.util.Optional;

public interface NotificationService {
    
    List<Notification> getNotifications(NotificationType type, Long incidentId, Long eventId);
    
    Optional<Notification> getNotificationById(Long id);
    
    Notification createNotification(NotificationCreateDTO dto);
    
    void deleteNotification(Long id);
    
    Optional<Notification> updateNotification(Long id, NotificationUpdateDTO dto);
}
