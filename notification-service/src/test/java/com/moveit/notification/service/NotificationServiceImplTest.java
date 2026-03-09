package com.moveit.notification.service;

import com.moveit.notification.dto.NotificationCreateDTO;
import com.moveit.notification.dto.NotificationUpdateDTO;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.TargetType;
import com.moveit.notification.repository.NotificationRepository;
import com.moveit.notification.service.impl.NotificationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SseEmitterService sseEmitterService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateNotification() {
        // Given
        NotificationCreateDTO dto = new NotificationCreateDTO(
            "Titre", 
            "Contenu", 
            NotificationType.ASSIGNMENT,
            TargetType.GLOBAL,
            null
        );
        Notification notif = new Notification();
        notif.setId(1L);
        notif.setTitle(dto.getTitle());
        notif.setContent(dto.getContent());
        notif.setNotificationType(dto.getNotificationType());
        notif.setTargetType(dto.getTargetType());
        when(notificationRepository.save(any(Notification.class))).thenReturn(notif);

        // When
        Notification result = notificationService.createNotification(dto);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Titre");
        assertThat(result.getContent()).isEqualTo("Contenu");
        assertThat(result.getNotificationType()).isEqualTo(NotificationType.ASSIGNMENT);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testCreateNotificationWithTarget() {
        // Given
        NotificationCreateDTO dto = new NotificationCreateDTO(
            "Competition Alert",
            "Content",
            NotificationType.START,
            TargetType.COMPETITION,
            42L
        );
        Notification notif = new Notification();
        notif.setId(10L);
        notif.setTitle(dto.getTitle());
        notif.setTargetType(TargetType.COMPETITION);
        notif.setTargetId(42L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notif);

        // When
        Notification result = notificationService.createNotification(dto);

        // Then
        assertThat(result.getTargetType()).isEqualTo(TargetType.COMPETITION);
        assertThat(result.getTargetId()).isEqualTo(42L);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testCreateNotificationWithChampionshipTarget() {
        // Given
        NotificationCreateDTO dto = new NotificationCreateDTO(
            "Championship Alert",
            "Content",
            NotificationType.RESULT,
            TargetType.CHAMPIONSHIP,
            99L
        );
        Notification notif = new Notification();
        notif.setId(11L);
        notif.setTitle(dto.getTitle());
        notif.setTargetType(TargetType.CHAMPIONSHIP);
        notif.setTargetId(99L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notif);

        // When
        Notification result = notificationService.createNotification(dto);

        // Then
        assertThat(result.getTargetType()).isEqualTo(TargetType.CHAMPIONSHIP);
        assertThat(result.getTargetId()).isEqualTo(99L);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testGetNotificationById() {
        // Given
        Notification notif = new Notification();
        notif.setId(2L);
        notif.setTitle("Test");
        when(notificationRepository.findById(2L)).thenReturn(Optional.of(notif));

        // When
        Optional<Notification> result = notificationService.getNotificationById(2L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(2L);
        assertThat(result.get().getTitle()).isEqualTo("Test");
        verify(notificationRepository, times(1)).findById(2L);
    }

    @Test
    void testGetNotificationById_NotFound() {
        // Given
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Notification> result = notificationService.getNotificationById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(notificationRepository, times(1)).findById(999L);
    }

    @Test
    void testGetNotificationsWithoutFilters() {
        // Given
        Notification notif = new Notification();
        notif.setId(3L);
        notif.setNotificationType(NotificationType.REMINDER);
        List<Notification> notifList = List.of(notif);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(notifList, pageable, 1);
        when(notificationRepository.findByFilters(null, null, null, pageable)).thenReturn(page);

        // When
        Page<Notification> result = notificationService.getNotifications(null, null, null, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(3L);
        verify(notificationRepository, times(1)).findByFilters(null, null, null, pageable);
    }

    @Test
    void testGetNotificationsByType() {
        // Given
        Notification notif = new Notification();
        notif.setId(4L);
        notif.setNotificationType(NotificationType.ALERT);
        List<Notification> notifList = List.of(notif);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(notifList, pageable, 1);
        when(notificationRepository.findByFilters(NotificationType.ALERT, null, null, pageable)).thenReturn(page);

        // When
        Page<Notification> result = notificationService.getNotifications(NotificationType.ALERT, null, null, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getNotificationType()).isEqualTo(NotificationType.ALERT);
        verify(notificationRepository, times(1)).findByFilters(NotificationType.ALERT, null, null, pageable);
    }

    @Test
    void testGetNotificationsByTargetType() {
        // Given
        Notification notif = new Notification();
        notif.setId(5L);
        notif.setTargetType(TargetType.COMPETITION);
        notif.setTargetId(100L);
        List<Notification> notifList = List.of(notif);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(notifList, pageable, 1);
        when(notificationRepository.findByFilters(null, TargetType.COMPETITION, null, pageable)).thenReturn(page);

        // When
        Page<Notification> result = notificationService.getNotifications(null, TargetType.COMPETITION, null, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTargetType()).isEqualTo(TargetType.COMPETITION);
        verify(notificationRepository, times(1)).findByFilters(null, TargetType.COMPETITION, null, pageable);
    }

    @Test
    void testGetNotificationsByTargetId() {
        // Given
        Notification notif = new Notification();
        notif.setId(6L);
        notif.setTargetType(TargetType.CHAMPIONSHIP);
        notif.setTargetId(200L);
        List<Notification> notifList = List.of(notif);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(notifList, pageable, 1);
        when(notificationRepository.findByFilters(null, null, 200L, pageable)).thenReturn(page);

        // When
        Page<Notification> result = notificationService.getNotifications(null, null, 200L, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTargetId()).isEqualTo(200L);
        verify(notificationRepository, times(1)).findByFilters(null, null, 200L, pageable);
    }

    @Test
    void testGetNotificationsWithMultipleFilters() {
        // Given
        Notification notif1 = new Notification();
        notif1.setId(7L);
        notif1.setNotificationType(NotificationType.ASSIGNMENT);
        notif1.setTargetType(TargetType.COMPETITION);
        notif1.setTargetId(100L);

        List<Notification> filteredNotifs = List.of(notif1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(filteredNotifs, pageable, 1);
        when(notificationRepository.findByFilters(NotificationType.ASSIGNMENT, TargetType.COMPETITION, null, pageable)).thenReturn(page);

        // When
        Page<Notification> result = notificationService.getNotifications(NotificationType.ASSIGNMENT, TargetType.COMPETITION, null, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(7L);
        assertThat(result.getContent().get(0).getNotificationType()).isEqualTo(NotificationType.ASSIGNMENT);
        verify(notificationRepository, times(1)).findByFilters(NotificationType.ASSIGNMENT, TargetType.COMPETITION, null, pageable);
    }

    @Test
    void testUpdateNotification() {
        // Given
        Notification existingNotif = new Notification();
        existingNotif.setId(10L);
        existingNotif.setTitle("Old Title");
        existingNotif.setContent("Old Content");
        
        NotificationUpdateDTO dto = new NotificationUpdateDTO();
        dto.setTitle("New Title");
        dto.setContent("New Content");
        
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(existingNotif));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        Optional<Notification> result = notificationService.updateNotification(10L, dto);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("New Title");
        assertThat(result.get().getContent()).isEqualTo("New Content");
        verify(notificationRepository, times(1)).findById(10L);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testUpdateNotificationPartial() {
        // Given
        Notification existingNotif = new Notification();
        existingNotif.setId(11L);
        existingNotif.setTitle("Old Title");
        existingNotif.setContent("Old Content");
        
        NotificationUpdateDTO dto = new NotificationUpdateDTO();
        dto.setTitle("Updated Title");
        // content reste null, ne doit pas être modifié
        
        when(notificationRepository.findById(11L)).thenReturn(Optional.of(existingNotif));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        Optional<Notification> result = notificationService.updateNotification(11L, dto);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Updated Title");
        assertThat(result.get().getContent()).isEqualTo("Old Content"); // Inchangé
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testUpdateNotification_NotFound() {
        // Given
        NotificationUpdateDTO dto = new NotificationUpdateDTO();
        dto.setTitle("New Title");
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Notification> result = notificationService.updateNotification(999L, dto);

        // Then
        assertThat(result).isEmpty();
        verify(notificationRepository, times(1)).findById(999L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testDeleteNotification() {
        // Given
        when(notificationRepository.existsById(15L)).thenReturn(true);
        doNothing().when(notificationRepository).deleteById(15L);

        // When
        notificationService.deleteNotification(15L);

        // Then
        verify(notificationRepository, times(1)).existsById(15L);
        verify(notificationRepository, times(1)).deleteById(15L);
    }

    @Test
    void testDeleteNotification_NotFound() {
        // Given
        when(notificationRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> notificationService.deleteNotification(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Notification non trouvée avec l'id: 999");
        
        verify(notificationRepository, times(1)).existsById(999L);
        verify(notificationRepository, never()).deleteById(anyLong());
    }
}
