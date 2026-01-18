package com.moveit.notification.service.impl;

import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.Subscription;
import com.moveit.notification.repository.SubscriptionRepository;
import com.moveit.notification.service.NotificationDispatcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationDispatcherServiceImpl implements NotificationDispatcherService {

    private final SubscriptionRepository subscriptionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void dispatch(Notification notification) {
        // Find all active subscriptions for this notification type
        List<Subscription> subscriptions = subscriptionRepository
                .findByNotificationType(notification.getNotificationType())
                .stream()
                .filter(Subscription::getActive)
                .toList();

        // Send to each subscribed user
        for (Subscription subscription : subscriptions) {
            String userId = subscription.getUserId();
            
            try {
                // Send via WebSocket (real-time push to connected clients)
                sendViaWebSocket(userId, notification);
                
            } catch (Exception e) {
                // Silent fail
            }
        }

        // Also broadcast to general topic for all connected clients
        broadcastToAll(notification);
    }

    private void sendViaWebSocket(String userId, Notification notification) {
        try {
            // Send to specific user's queue
            messagingTemplate.convertAndSend(
                    "/queue/notifications/" + userId,
                    notification
            );
        } catch (Exception e) {
            // Silent fail
        }
    }

    private void broadcastToAll(Notification notification) {
        try {
            // Broadcast to all connected clients on the topic
            messagingTemplate.convertAndSend(
                    "/topic/notifications",
                    notification
            );
        } catch (Exception e) {
            // Silent fail
        }
    }
}
