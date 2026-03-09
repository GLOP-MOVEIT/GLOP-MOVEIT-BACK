package com.moveit.notification.service;

import com.moveit.notification.dto.NotificationCreateDTO;
import com.moveit.notification.dto.NotificationUpdateDTO;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationService {
    
    Page<Notification> getNotifications(NotificationType type, TargetType targetType, Long targetId, Pageable pageable);

    Optional<Notification> getNotificationById(Long id);
    
    Notification createNotification(NotificationCreateDTO dto);
    
    void deleteNotification(Long id);
    
    Optional<Notification> updateNotification(Long id, NotificationUpdateDTO dto);
}
