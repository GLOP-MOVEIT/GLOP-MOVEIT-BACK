package com.moveit.notification.repository;

import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserId(String userId);
    List<Subscription> findByNotificationType(NotificationType notificationType);
    Optional<Subscription> findByUserIdAndNotificationType(String userId, NotificationType notificationType);

    /**
     * Point P1 - Query optimisée : récupère directement les userIds actifs
     * Évite de charger toutes les entités Subscription en mémoire
     */
    @Query("SELECT s.userId FROM Subscription s WHERE s.notificationType = :type AND s.active = true")
    List<String> findActiveUserIdsByNotificationType(@Param("type") NotificationType type);
}
