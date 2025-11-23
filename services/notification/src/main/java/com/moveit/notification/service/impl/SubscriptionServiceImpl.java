package com.moveit.notification.service.impl;

import com.moveit.notification.dto.SubscribeRequest;
import com.moveit.notification.dto.SubscriptionResponse;
import com.moveit.notification.entity.NotificationSubscription;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.repository.NotificationSubscriptionRepository;
import com.moveit.notification.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final NotificationSubscriptionRepository subscriptionRepository;

    @Override
    public SubscriptionResponse subscribe(Long userId, SubscribeRequest request) {
        log.info("Creating subscription for user {} - type:{}, level:{}, topic:{}",
                userId, request.getTypeName(), request.getLevelName(), request.getTopic());

        // Vérifier si le type est mandatory - si oui, l'user ne peut pas opt-out
        // (mais peut créer une subscription active)
        if (request.getTypeName() != null && !request.getActive()) {
            NotificationType type = NotificationType.valueOf(request.getTypeName());
            if (type.isMandatory()) {
                throw new IllegalArgumentException(
                        "Cannot create inactive subscription for mandatory type: " + request.getTypeName());
            }
        }

        NotificationSubscription subscription = NotificationSubscription.builder()
                .userId(userId)
                .typeName(request.getTypeName())
                .levelName(request.getLevelName())
                .topic(request.getTopic())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription created with ID: {}", subscription.getId());

        return mapToResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getUserSubscriptions(Long userId) {
        log.debug("Fetching subscriptions for user {}", userId);
        return subscriptionRepository.findByUserIdAndActiveTrue(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void unsubscribe(Long userId, Long subscriptionId) {
        log.info("Unsubscribing user {} from subscription {}", userId, subscriptionId);

        NotificationSubscription subscription = subscriptionRepository
                .findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Subscription not found or does not belong to user"));

        // Vérifier si c'est un type mandatory
        if (subscription.getTypeName() != null) {
            NotificationType type = NotificationType.valueOf(subscription.getTypeName());
            if (type.isMandatory()) {
                throw new IllegalArgumentException(
                        "Cannot unsubscribe from mandatory notification type: " + subscription.getTypeName());
            }
        }

        // Soft delete: mettre active à false
        subscription.setActive(false);
        subscriptionRepository.save(subscription);
        log.info("Subscription {} deactivated", subscriptionId);
    }

    @Override
    public void deleteSubscription(Long userId, Long subscriptionId) {
        log.info("Deleting subscription {} for user {}", subscriptionId, userId);

        NotificationSubscription subscription = subscriptionRepository
                .findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Subscription not found or does not belong to user"));

        // Vérifier mandatory type
        if (subscription.getTypeName() != null) {
            NotificationType type = NotificationType.valueOf(subscription.getTypeName());
            if (type.isMandatory()) {
                throw new IllegalArgumentException(
                        "Cannot delete subscription for mandatory notification type: " + subscription.getTypeName());
            }
        }

        subscriptionRepository.delete(subscription);
        log.info("Subscription {} deleted", subscriptionId);
    }

    private SubscriptionResponse mapToResponse(NotificationSubscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUserId())
                .typeName(subscription.getTypeName())
                .levelName(subscription.getLevelName())
                .topic(subscription.getTopic())
                .active(subscription.getActive())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }
}
