package com.moveit.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Notification (message central).
 * Peut avoir plusieurs destinataires via NotificationRecipient (many-to-many).
 * Évite la duplication du contenu pour les envois massifs.
 */
@Entity
@Table(name = "notification", indexes = {
        @Index(name = "idx_notification_type", columnList = "notification_type"),
        @Index(name = "idx_notification_level", columnList = "level_id"),
        @Index(name = "idx_notification_start_date", columnList = "notification_start_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private NotificationLevel level;

    @Column(name = "notification_security")
    private Integer securityId;

    @Column(name = "notification_competition")
    private Integer competitionId;

    @Column(name = "notification_name", nullable = false, length = 255)
    private String name;

    @Column(name = "notification_body", columnDefinition = "TEXT")
    private String body;

    @CreationTimestamp
    @Column(name = "notification_start_date", nullable = false, updatable = false)
    private Instant startDate;
}
