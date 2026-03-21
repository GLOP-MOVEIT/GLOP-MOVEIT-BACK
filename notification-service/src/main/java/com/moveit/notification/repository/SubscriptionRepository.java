package com.moveit.notification.repository;

import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.Subscription;
import com.moveit.notification.entity.TargetType;
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

    Optional<Subscription> findByUserIdAndNotificationTypeAndTargetTypeAndTargetId(
            String userId, NotificationType notificationType, TargetType targetType, Long targetId);

    /**
     * Récupère les userIds actifs pour un type de notification GLOBAL.
     */
    @Query("SELECT s.userId FROM Subscription s WHERE s.notificationType = :type AND s.targetType = 'GLOBAL' AND s.active = true")
    List<String> findActiveUserIdsForGlobal(@Param("type") NotificationType type);

    /**
     * Récupère les userIds actifs pour un type + target spécifique.
     * Inclut aussi les abonnés GLOBAL du même type (ils reçoivent tout).
     */
    @Query("SELECT DISTINCT s.userId FROM Subscription s WHERE s.notificationType = :type AND s.active = true " +
           "AND (s.targetType = 'GLOBAL' OR (s.targetType = :targetType AND s.targetId = :targetId))")
    List<String> findActiveUserIdsForTarget(
            @Param("type") NotificationType type,
            @Param("targetType") TargetType targetType,
            @Param("targetId") Long targetId);

    /**
     * Récupère les subscriptions actives d'un user.
     * Utilisé à la reconnexion SSE pour filtrer les notifs manquées.
     */
    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.active = true")
    List<Subscription> findActiveSubscriptionsByUserId(@Param("userId") String userId);
}
