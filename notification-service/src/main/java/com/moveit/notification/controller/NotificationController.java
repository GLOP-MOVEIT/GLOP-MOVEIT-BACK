package com.moveit.notification.controller;

import com.moveit.notification.config.PaginationConfig;
import com.moveit.notification.dto.NotificationCreateDTO;
import com.moveit.notification.dto.NotificationResponseDTO;
import com.moveit.notification.dto.NotificationUpdateDTO;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.exception.InvalidPaginationException;
import com.moveit.notification.exception.InvalidSortFieldException;
import com.moveit.notification.mapper.NotificationMapper;
import com.moveit.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final PaginationConfig paginationConfig;
    
    // Whitelist des champs autorisés pour le tri (sécurité contre injection SQL)
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "title", "notificationType", "createdAt"
    );

    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getNotifications(
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Long incidentId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        // Validation du champ de tri (Point 7 - Sécurité)
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new InvalidSortFieldException(
                "Invalid sort field: '" + sortBy + "'. Allowed fields: " + ALLOWED_SORT_FIELDS
            );
        }
        
        // Validation de la pagination (Point 10 - Limites)
        validatePagination(page, size);
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<Notification> notifications = notificationService.getNotifications(type, incidentId, eventId, pageable);
        Page<NotificationResponseDTO> response = notifications.map(notificationMapper::toResponseDTO);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Valide les paramètres de pagination.
     * Point 10 - Évite les requêtes massives en limitant la taille des pages.
     */
    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new InvalidPaginationException("Page number must be >= 0, got: " + page);
        }
        
        if (size < paginationConfig.getMinPageSize() || size > paginationConfig.getMaxPageSize()) {
            throw new InvalidPaginationException(
                "Page size must be between " + paginationConfig.getMinPageSize() + 
                " and " + paginationConfig.getMaxPageSize() + 
                ", got: " + size
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> getNotificationById(@PathVariable Long id) {
        return notificationService.getNotificationById(id)
                .map(notificationMapper::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<NotificationResponseDTO> createNotification(@Valid @RequestBody NotificationCreateDTO dto) {
        Notification saved = notificationService.createNotification(dto);
        return ResponseEntity.ok(notificationMapper.toResponseDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> updateNotification(@PathVariable Long id, @Valid @RequestBody NotificationUpdateDTO dto) {
        return notificationService.updateNotification(id, dto)
                .map(notificationMapper::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
