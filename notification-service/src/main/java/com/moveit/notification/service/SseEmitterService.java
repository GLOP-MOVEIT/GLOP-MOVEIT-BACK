package com.moveit.notification.service;

import com.moveit.notification.dto.NotificationResponseDTO;
import com.moveit.notification.entity.Notification;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Service de gestion des connexions SSE (Server-Sent Events).
 *
 * Responsabilités :
 * - Gérer les connexions SSE des utilisateurs (abonnement/déconnexion)
 * - Envoyer des notifications en temps réel via SSE
 * - Gérer la reconnexion avec replay des notifications manquées (Last-Event-ID)
 * - Broadcaster des notifications à tous les utilisateurs abonnés
 */
public interface SseEmitterService {

    /**
     * Crée et enregistre un nouvel emitter SSE pour un utilisateur.
     *
     * @param userId identifiant de l'utilisateur
     * @param lastEventId dernière notification reçue par le client (header Last-Event-ID).
     *                    Si non null, renvoie toutes les notifs manquées depuis cet ID.
     * @return SseEmitter configuré pour cet utilisateur
     */
    SseEmitter subscribe(String userId, Long lastEventId);

    /**
     * Envoie une notification à un utilisateur spécifique via SSE.
     * Si l'utilisateur a plusieurs connexions actives (plusieurs onglets/devices),
     * la notification est envoyée à toutes.
     *
     * @param userId identifiant de l'utilisateur destinataire
     * @param dto notification à envoyer
     */
    void sendToUser(String userId, NotificationResponseDTO dto);

    /**
     * Broadcaster une notification à tous les utilisateurs abonnés à son type.
     * Appelé après la création d'une notification pour dispatch en temps réel via SSE.
     *
     * @param notification la notification à broadcaster
     */
    void broadcastToSubscribers(Notification notification);
}

