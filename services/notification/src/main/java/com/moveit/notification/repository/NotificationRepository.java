package com.moveit.notification.repository;

import com.moveit.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository pour les notifications (messages centraux).
 * Note: userId et read ont été déplacés vers NotificationRecipient.
 * Les queries user-specific doivent passer par NotificationRecipientRepository.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Les queries basiques sont héritées de JpaRepository
    // Pour queries user-specific, utiliser NotificationRecipientRepository
}
