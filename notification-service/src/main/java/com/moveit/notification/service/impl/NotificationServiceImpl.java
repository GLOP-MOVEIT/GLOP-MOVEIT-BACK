package com.moveit.notification.service.impl;

import com.moveit.notification.dto.NotificationCreateDTO;
import com.moveit.notification.dto.NotificationUpdateDTO;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.TargetType;
import com.moveit.notification.repository.NotificationRepository;
import com.moveit.notification.service.NotificationService;
import com.moveit.notification.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;

    @Override
    public Page<Notification> getNotifications(NotificationType type, TargetType targetType, Long targetId, Pageable pageable) {
        return notificationRepository.findByFilters(type, targetType, targetId, pageable);
    }

    @Override
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    @Override
    public Notification createNotification(NotificationCreateDTO dto) {
        validateTarget(dto.getTargetType(), dto.getTargetId());

        Notification notification = new Notification();
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        notification.setNotificationType(dto.getNotificationType());
        notification.setTargetType(dto.getTargetType());
        notification.setTargetId(dto.getTargetId());

        Notification savedNotification = notificationRepository.save(notification);

        sseEmitterService.broadcastToSubscribers(savedNotification);

        return savedNotification;
    }

    @Override
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new jakarta.persistence.EntityNotFoundException("Notification non trouvée avec l'id: " + id);
        }
        notificationRepository.deleteById(id);
    }

    @Override
    public Optional<Notification> updateNotification(Long id, NotificationUpdateDTO dto) {
        return notificationRepository.findById(id)
                .map(notification -> {
                    if (dto.getTitle() != null) {
                        notification.setTitle(dto.getTitle());
                    }
                    if (dto.getContent() != null) {
                        notification.setContent(dto.getContent());
                    }
                    return notificationRepository.save(notification);
                });
    }

    private void validateTarget(TargetType targetType, Long targetId) {
        if (targetType == TargetType.GLOBAL && targetId != null) {
            throw new IllegalArgumentException("targetId must be null when targetType is GLOBAL");
        }
        if (targetType != TargetType.GLOBAL && targetId == null) {
            throw new IllegalArgumentException("targetId is required when targetType is " + targetType);
        }
    }
}
