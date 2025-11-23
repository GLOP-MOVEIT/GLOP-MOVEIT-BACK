package com.moveit.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Abonnement d'un utilisateur aux notifications.
 * Permet de filtrer quels types/niveaux/topics de notifications il souhaite recevoir.
 * 
 * Tous les champs de filtre (typeName, levelName, topic) sont optionnels (NULL = tous).
 */
@Entity
@Table(name = "notification_subscription")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Type de notification (NotificationType.name()) ou NULL pour tous les types.
     */
    @Column(name = "subscription_type", length = 50)
    private String typeName;

    /**
     * Niveau de notification (CRITIQUE, ORGANISATIONNEL, INFORMATIONNEL) ou NULL pour tous.
     */
    @Column(name = "level_name", length = 50)
    private String levelName;

    /**
     * Topic custom (ex: "competition:42", "zone:5") ou NULL pour tous.
     */
    @Column(name = "topic", length = 100)
    private String topic;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
