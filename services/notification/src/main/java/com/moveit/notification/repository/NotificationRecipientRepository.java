package com.moveit.notification.repository;

import com.moveit.notification.entity.NotificationRecipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour les destinataires de notifications (nouveau modèle).
 */
@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {

    /**
     * Récupère les notifications reçues par un utilisateur avec pagination et filtres optionnels.
     * Utilise keyset pagination via afterId pour scroll infini.
     */
    @Query("SELECT r FROM NotificationRecipient r " +
           "JOIN FETCH r.notification n " +
           "JOIN FETCH n.level l " +
           "WHERE r.userId = :userId " +
           "AND (:levelName IS NULL OR l.name = :levelName) " +
           "AND (:read IS NULL OR r.read = :read) " +
           "AND (:afterId IS NULL OR r.id < :afterId) " +
           "ORDER BY l.priority ASC, r.createdAt DESC")
    Page<NotificationRecipient> findByUserIdWithFilters(
            @Param("userId") Long userId,
            @Param("levelName") String levelName,
            @Param("read") Boolean read,
            @Param("afterId") Long afterId,
            Pageable pageable
    );

    /**
     * Compte les notifications non lues d'un utilisateur.
     */
    long countByUserIdAndReadFalse(Long userId);

    /**
     * Récupère les notifications critiques non lues d'un utilisateur.
     */
    @Query("SELECT r FROM NotificationRecipient r " +
           "JOIN FETCH r.notification n " +
           "JOIN FETCH n.level l " +
           "WHERE r.userId = :userId " +
           "AND l.name = 'CRITIQUE' " +
           "AND r.read = false " +
           "ORDER BY r.createdAt DESC")
    List<NotificationRecipient> findCriticalUnreadByUserId(@Param("userId") Long userId);

    /**
     * Trouve un recipient spécifique par ID et userId (pour vérifier ownership).
     */
    Optional<NotificationRecipient> findByIdAndUserId(Long id, Long userId);

    /**
     * Marque toutes les notifications d'un utilisateur comme lues.
     * Utilise une requête SQL native pour pouvoir mettre à jour readAt avec NOW().
     * Retourne le nombre de lignes mises à jour.
     */
    @Modifying
    @Query(value = "UPDATE notification_recipient SET is_read = true, read_at = NOW() " +
                   "WHERE user_id = :userId AND is_read = false",
           nativeQuery = true)
    int markAllAsReadByUserId(@Param("userId") Long userId);
}
