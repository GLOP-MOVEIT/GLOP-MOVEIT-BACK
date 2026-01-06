package com.moveit.notification.controller;

import com.moveit.notification.entity.NotificationType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/notification/types")
public class NotificationTypeController {

    @GetMapping
    public ResponseEntity<List<NotificationType>> getAllTypes() {
        return ResponseEntity.ok(Arrays.asList(NotificationType.values()));
    }

    @GetMapping("/{type}")
    public ResponseEntity<NotificationType> getType(@PathVariable NotificationType type) {
        return ResponseEntity.ok(type);
    }
}
