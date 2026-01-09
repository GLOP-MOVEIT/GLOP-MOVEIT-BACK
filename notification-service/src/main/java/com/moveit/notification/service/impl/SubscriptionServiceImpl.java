package com.moveit.notification.service.impl;

import com.moveit.notification.dto.SubscriptionCreateDTO;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.Subscription;
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
        // Si aucun filtre, retourner tous
        if (userId == null && type == null) {
            return subscriptionRepository.findAll();
        }
        
        // Si filtre par userId uniquement
        if (userId != null && type == null) {
            return subscriptionRepository.findByUserId(userId);
        }
        
        // Si filtre par type uniquement
        if (type != null && userId == null) {
            return subscriptionRepository.findByNotificationType(type);
        }
        
        // Les deux filtres : filtrer manuellement
        return subscriptionRepository.findByUserId(userId).stream()
                .filter(s -> s.getNotificationType() == type)
                .toList();
    }

    @Override
    public Optional<Subscription> getSubscriptionById(Long id) {
        return subscriptionRepository.findById(id);
    }

    @Override
    public Subscription createSubscription(SubscriptionCreateDTO subscription) {
        // Vérifier si l'utilisateur est déjà abonné à ce type
        Optional<Subscription> existing = subscriptionRepository
                .findByUserIdAndNotificationType(
                        subscription.getUserId(),
                        subscription.getNotificationType()
                );
        
        if (existing.isPresent()) {
            // Réactiver l'abonnement s'il était désactivé
            Subscription existingSub = existing.get();
            existingSub.setActive(true);
            return subscriptionRepository.save(existingSub);
        }
        
        Subscription newSubscription = new Subscription();
        newSubscription.setUserId(subscription.getUserId());
        newSubscription.setNotificationType(subscription.getNotificationType());
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
}
