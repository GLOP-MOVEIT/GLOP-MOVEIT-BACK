package com.moveit.notification.repository;

import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationType;
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
    Page<Notification> findByIncidentIdsContaining(Long incidentId, Pageable pageable);
    Page<Notification> findByEventIdsContaining(Long eventId, Pageable pageable);
    
    // Versions sans pagination pour compatibilité
    List<Notification> findByNotificationType(NotificationType notificationType);
    List<Notification> findByIncidentIdsContaining(Long incidentId);
    List<Notification> findByEventIdsContaining(Long eventId);
    
    /**
     * Query optimisée pour gérer tous les filtres en une seule requête SQL
     * Évite de charger toutes les notifications en mémoire
     */
    @Query("""
        SELECT DISTINCT n FROM Notification n
        LEFT JOIN n.incidentIds inc
        LEFT JOIN n.eventIds evt
        WHERE (:type IS NULL OR n.notificationType = :type)
          AND (:incidentId IS NULL OR inc = :incidentId)
          AND (:eventId IS NULL OR evt = :eventId)
        """)
    Page<Notification> findByFilters(
        @Param("type") NotificationType type,
        @Param("incidentId") Long incidentId,
        @Param("eventId") Long eventId,
        Pageable pageable
    );
}
