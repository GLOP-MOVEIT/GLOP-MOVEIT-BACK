package com.moveit.notification.controller;

import com.moveit.notification.dto.SubscriptionCreateDTO;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.Subscription;
import com.moveit.notification.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<Subscription>> getSubscriptions(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) NotificationType type) {
        return ResponseEntity.ok(subscriptionService.getSubscriptions(userId, type));
    }

    @PostMapping
    public ResponseEntity<Subscription> createSubscription(@Valid @RequestBody SubscriptionCreateDTO dto) {
        Subscription saved = subscriptionService.createSubscription(dto);
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Subscription> toggleSubscription(@PathVariable Long id) {
        return subscriptionService.toggleSubscription(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }
}
