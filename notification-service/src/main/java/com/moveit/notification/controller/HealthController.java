package main.java.com.moveit.notification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notification")
public class HealthController {

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        log.info("Status endpoint called");
        Map<String, String> status = new HashMap<>();
        status.put("service", "notification-service");
        status.put("status", "UP");
        return ResponseEntity.ok(status);
    }
}
