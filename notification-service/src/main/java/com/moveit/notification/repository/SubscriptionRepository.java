package com.moveit.notification.repository;

import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserId(String userId);
    List<Subscription> findByNotificationType(NotificationType notificationType);
    Optional<Subscription> findByUserIdAndNotificationType(String userId, NotificationType notificationType);

    /**
     * Récupère les userIds actifs pour un type de notification donné.
     * Utilisé par le dispatcher pour savoir à qui envoyer la notif en temps réel.
     */
    @Query("SELECT s.userId FROM Subscription s WHERE s.notificationType = :type AND s.active = true")
    List<String> findActiveUserIdsByNotificationType(@Param("type") NotificationType type);

    /**
     * Récupère les types de notification actifs pour un userId donné.
     * Utilisé à la reconnexion SSE pour filtrer les notifs manquées.
     */
    @Query("SELECT s.notificationType FROM Subscription s WHERE s.userId = :userId AND s.active = true")
    Set<NotificationType> findActiveNotificationTypesByUserId(@Param("userId") String userId);
}
