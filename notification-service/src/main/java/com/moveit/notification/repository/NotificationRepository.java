package com.moveit.notification.repository;

import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByNotificationType(NotificationType notificationType, Pageable pageable);
    List<Notification> findByNotificationType(NotificationType notificationType);

    /**
     * Récupère les notifications manquées depuis un ID donné (reconnexion SSE).
     */
    List<Notification> findByIdGreaterThanOrderByIdAsc(Long lastId);

    /**
     * Query optimisée pour gérer tous les filtres en une seule requête SQL.
     */
    @Query("""
        SELECT n FROM Notification n
        WHERE (:type IS NULL OR n.notificationType = :type)
          AND (:targetType IS NULL OR n.targetType = :targetType)
          AND (:targetId IS NULL OR n.targetId = :targetId)
        """)
    Page<Notification> findByFilters(
        @Param("type") NotificationType type,
        @Param("targetType") TargetType targetType,
        @Param("targetId") Long targetId,
        Pageable pageable
    );
}
