package com.moveit.notification.repository;

import com.moveit.notification.entity.NotificationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository pour les abonnements aux notifications.
 */
@Repository
public interface NotificationSubscriptionRepository extends JpaRepository<NotificationSubscription, Long> {

    /**
     * Récupère toutes les subscriptions actives d'un utilisateur.
     */
    List<NotificationSubscription> findByUserIdAndActiveTrue(Long userId);

    /**
     * Trouve une subscription par ID et userId (pour vérifier ownership avant delete).
     */
    Optional<NotificationSubscription> findByIdAndUserId(Long id, Long userId);

    /**
     * Résout les userIds abonnés pour un type/level/topic donné.
     * 
     * Logique: un user est inclus si il a une subscription active ET que :
     * - (sa subscription a typeName NULL OU typeName correspond)
     * - (sa subscription a levelName NULL OU levelName correspond)  
     * - (sa subscription a topic NULL OU topic correspond)
     * 
     * Cela permet des subscriptions larges (NULL = tous) ou spécifiques.
     */
    @Query("SELECT DISTINCT s.userId FROM NotificationSubscription s " +
           "WHERE s.active = true " +
           "AND (:typeName IS NULL OR s.typeName IS NULL OR s.typeName = :typeName) " +
           "AND (:levelName IS NULL OR s.levelName IS NULL OR s.levelName = :levelName) " +
           "AND (:topic IS NULL OR s.topic IS NULL OR s.topic = :topic)")
    Set<Long> findSubscribedUserIds(
            @Param("typeName") String typeName,
            @Param("levelName") String levelName,
            @Param("topic") String topic
    );

    /**
     * Vérifie si un user a au moins une subscription active.
     */
    boolean existsByUserIdAndActiveTrue(Long userId);
}
