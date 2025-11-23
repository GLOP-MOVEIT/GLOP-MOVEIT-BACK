package com.moveit.notification.service;

import com.moveit.notification.dto.NotificationListResponse;
import com.moveit.notification.dto.NotificationRequest;
import com.moveit.notification.dto.NotificationResponse;

import java.util.List;

/**
 * Interface du service de gestion des notifications.
 */
public interface NotificationService {

    /**
     * Crée une nouvelle notification et l'envoie en temps réel via WebSocket.
     *
     * @param request Données de la notification
     * @return La notification créée
     */
    NotificationResponse createNotification(NotificationRequest request);

    /**
     * Récupère les notifications d'un utilisateur avec scroll infini.
     *
     * @param userId     ID de l'utilisateur
     * @param levelName  Filtre par niveau (optionnel)
     * @param read       Filtre par statut lu/non-lu (optionnel)
     * @param limit      Nombre de notifications à retourner
     * @param afterId    ID de la dernière notification chargée (curseur, optionnel)
     * @return Liste de notifications avec métadonnées
     */
    NotificationListResponse getUserNotifications(Long userId, String levelName, Boolean read, int limit, Long afterId);

    /**
     * Récupère les notifications critiques non lues d'un utilisateur (SLA < 30s).
     *
     * @param userId ID de l'utilisateur
     * @return Liste des notifications critiques non lues
     */
    List<NotificationResponse> getCriticalAlerts(Long userId);

    /**
     * Marque une notification comme lue ou non lue.
     *
     * @param notificationId ID de la notification
     * @param read           Nouveau statut (true = lue, false = non lue)
     * @return La notification mise à jour
     */
    NotificationResponse markAsRead(Long notificationId, Boolean read);

    /**
     * Marque toutes les notifications d'un utilisateur comme lues.
     *
     * @param userId ID de l'utilisateur
     * @return Nombre de notifications marquées comme lues
     */
    int markAllAsRead(Long userId);

    /**
     * Compte les notifications non lues d'un utilisateur (pour le badge).
     *
     * @param userId ID de l'utilisateur
     * @return Nombre de notifications non lues
     */
    long countUnreadNotifications(Long userId);
}
