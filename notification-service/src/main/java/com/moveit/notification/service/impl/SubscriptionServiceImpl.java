package com.moveit.notification.service.impl;

import com.moveit.notification.dto.SubscriptionCreateDTO;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.Subscription;
import com.moveit.notification.entity.TargetType;
import com.moveit.notification.repository.SubscriptionRepository;
import com.moveit.notification.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public List<Subscription> getSubscriptions(String userId, NotificationType type) {
        if (userId == null && type == null) {
            return subscriptionRepository.findAll();
        }
        if (userId != null && type == null) {
            return subscriptionRepository.findByUserId(userId);
        }
        if (userId == null) {
            return subscriptionRepository.findByNotificationType(type);
        }
        return subscriptionRepository.findByUserId(userId).stream()
                .filter(s -> s.getNotificationType() == type)
                .toList();
    }

    @Override
    public Optional<Subscription> getSubscriptionById(Long id) {
        return subscriptionRepository.findById(id);
    }

    @Override
    public Subscription createSubscription(SubscriptionCreateDTO dto) {
        validateTarget(dto.getTargetType(), dto.getTargetId());

        // Vérifier si l'utilisateur est déjà abonné à ce type + target
        Optional<Subscription> existing = subscriptionRepository
                .findByUserIdAndNotificationTypeAndTargetTypeAndTargetId(
                        dto.getUserId(),
                        dto.getNotificationType(),
                        dto.getTargetType(),
                        dto.getTargetId()
                );

        if (existing.isPresent()) {
            Subscription existingSub = existing.get();
            if (!existingSub.getActive()) {
                existingSub.setActive(true);
                return subscriptionRepository.save(existingSub);
            }
            return existingSub;
        }

        Subscription newSubscription = new Subscription();
        newSubscription.setUserId(dto.getUserId());
        newSubscription.setNotificationType(dto.getNotificationType());
        newSubscription.setTargetType(dto.getTargetType());
        newSubscription.setTargetId(dto.getTargetId());
        newSubscription.setActive(true);

        return subscriptionRepository.save(newSubscription);
    }

    @Override
    public void deleteSubscription(Long id) {
        if (!subscriptionRepository.existsById(id)) {
            throw new jakarta.persistence.EntityNotFoundException("Subscription non trouvée avec l'id: " + id);
        }
        subscriptionRepository.deleteById(id);
    }

    @Override
    public Optional<Subscription> toggleSubscription(Long id) {
        return subscriptionRepository.findById(id)
                .map(subscription -> {
                    subscription.setActive(!subscription.getActive());
                    return subscriptionRepository.save(subscription);
                });
    }

    private void validateTarget(TargetType targetType, Long targetId) {
        if (targetType == TargetType.GLOBAL && targetId != null) {
            throw new IllegalArgumentException("targetId must be null when targetType is GLOBAL");
        }
        if (targetType != TargetType.GLOBAL && targetId == null) {
            throw new IllegalArgumentException("targetId is required when targetType is " + targetType);
        }
    }
}
