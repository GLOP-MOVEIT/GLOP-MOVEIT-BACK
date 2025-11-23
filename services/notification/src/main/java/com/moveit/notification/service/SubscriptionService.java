package com.moveit.notification.service;

import com.moveit.notification.dto.SubscribeRequest;
import com.moveit.notification.dto.SubscriptionResponse;

import java.util.List;

/**
 * Service de gestion des abonnements aux notifications.
 */
public interface SubscriptionService {

    /**
     * Crée un nouvel abonnement pour l'utilisateur.
     */
    SubscriptionResponse subscribe(Long userId, SubscribeRequest request);

    /**
     * Récupère tous les abonnements actifs de l'utilisateur.
     */
    List<SubscriptionResponse> getUserSubscriptions(Long userId);

    /**
     * Désabonne l'utilisateur (soft delete en mettant active=false).
     * Vérifie que le type n'est pas mandatory avant de permettre la désactivation.
     */
    void unsubscribe(Long userId, Long subscriptionId);

    /**
     * Supprime définitivement un abonnement.
     */
    void deleteSubscription(Long userId, Long subscriptionId);
}
