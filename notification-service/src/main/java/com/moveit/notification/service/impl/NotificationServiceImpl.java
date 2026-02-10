package com.moveit.notification.service.impl;

import com.moveit.notification.dto.NotificationCreateDTO;
import com.moveit.notification.dto.NotificationUpdateDTO;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.repository.NotificationRepository;
import com.moveit.notification.service.NotificationDispatcherService;
import com.moveit.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDispatcherService dispatcherService;

    @Override
    public Page<Notification> getNotifications(NotificationType type, Long incidentId, Long eventId, Pageable pageable) {
        // Utilise la query optimisée qui gère tous les cas de filtrage en SQL
        // Évite de charger toutes les notifications en mémoire
        return notificationRepository.findByFilters(type, incidentId, eventId, pageable);
    }

    @Override
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    @Override
    public Notification createNotification(NotificationCreateDTO dto) {
        Notification notification = new Notification();
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        notification.setNotificationType(dto.getNotificationType());
        notification.setIncidentIds(dto.getIncidentIds());
        notification.setEventIds(dto.getEventIds());
        
        // Save to database
        Notification savedNotification = notificationRepository.save(notification);
        
        // Dispatch notification to subscribed users via WebSocket
        dispatcherService.dispatch(savedNotification);
        
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
                    if (dto.getIncidentIds() != null) {
                        notification.setIncidentIds(dto.getIncidentIds());
                    }
                    if (dto.getEventIds() != null) {
                        notification.setEventIds(dto.getEventIds());
                    }
                    return notificationRepository.save(notification);
                });
    }
}
