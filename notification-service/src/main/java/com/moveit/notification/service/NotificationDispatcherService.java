package com.moveit.notification.service;

import com.moveit.notification.entity.Notification;

public interface NotificationDispatcherService {
    
    /**
     * Dispatch notification to all subscribed users
     * Sends via WebSocket (real-time) and Email based on user subscriptions
     * @param notification the notification to dispatch
     */
    void dispatch(Notification notification);
}
