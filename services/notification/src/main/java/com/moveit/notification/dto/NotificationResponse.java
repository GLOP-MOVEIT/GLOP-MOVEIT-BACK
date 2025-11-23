package com.moveit.notification.dto;

import com.moveit.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO pour retourner une notification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private Long userId;
    private NotificationType type;
    private String typeName; // Nom du type pour affichage
    private String typeDescription; // Description traduite (optionnel)
    
    private String levelName; // CRITIQUE, ORGANISATIONNEL, INFORMATIONNEL
    private Integer levelPriority;
    
    private String name;
    private String body;
    
    private Integer securityId;
    private Integer competitionId;
    
    private Boolean read;
    private Instant createdAt;
    
    private Boolean mandatory; // Notification essentielle (non désactivable)
}
