package com.moveit.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Destinataire d'une notification.
 * Une ligne par user ayant reçu la notification (permet tracking lecture individuel).
 */
@Entity
@Table(name = "notification_recipient")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipient_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    @Column(name = "read_at")
    private Instant readAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Constructeur helper pour création rapide lors d'envois massifs
    public NotificationRecipient(Notification notification, Long userId) {
        this.notification = notification;
        this.userId = userId;
        this.read = false;
    }
}
