package com.moveit.notification.service;

import com.moveit.notification.dto.SubscriptionCreateDTO;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionService {
    
    List<Subscription> getSubscriptions(String userId, NotificationType type);
    
    Optional<Subscription> getSubscriptionById(Long id);
    
    Subscription createSubscription(SubscriptionCreateDTO dto);
    
    void deleteSubscription(Long id);
    
    Optional<Subscription> toggleSubscription(Long id);
}
