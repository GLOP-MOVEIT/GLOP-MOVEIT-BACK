package com.moveit.notification.service;

import com.moveit.notification.dto.SubscriptionCreateDTO;
import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.Subscription;
import com.moveit.notification.entity.TargetType;
import com.moveit.notification.repository.SubscriptionRepository;
import com.moveit.notification.service.impl.SubscriptionServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Subscription buildSubscription(Long id, String userId, NotificationType type, TargetType targetType, Long targetId, Boolean active) {
        Subscription sub = new Subscription();
        sub.setId(id);
        sub.setUserId(userId);
        sub.setNotificationType(type);
        sub.setTargetType(targetType);
        sub.setTargetId(targetId);
        sub.setActive(active);
        return sub;
    }

    @Test
    void testGetSubscriptionsWithoutFilters() {
        Subscription sub1 = buildSubscription(1L, "user1", NotificationType.ASSIGNMENT, TargetType.GLOBAL, null, true);
        Subscription sub2 = buildSubscription(2L, "user2", NotificationType.ALERT, TargetType.GLOBAL, null, true);
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub1, sub2));

        List<Subscription> result = subscriptionService.getSubscriptions(null, null);

        assertThat(result).hasSize(2);
        verify(subscriptionRepository).findAll();
    }

    @Test
    void testGetSubscriptionsByUserId() {
        Subscription sub1 = buildSubscription(1L, "user1", NotificationType.ASSIGNMENT, TargetType.GLOBAL, null, true);
        Subscription sub2 = buildSubscription(2L, "user1", NotificationType.ALERT, TargetType.GLOBAL, null, true);
        when(subscriptionRepository.findByUserId("user1")).thenReturn(List.of(sub1, sub2));

        List<Subscription> result = subscriptionService.getSubscriptions("user1", null);

        assertThat(result).hasSize(2).allMatch(s -> s.getUserId().equals("user1"));
        verify(subscriptionRepository).findByUserId("user1");
    }

    @Test
    void testGetSubscriptionsByType() {
        Subscription sub1 = buildSubscription(1L, "user1", NotificationType.ASSIGNMENT, TargetType.GLOBAL, null, true);
        when(subscriptionRepository.findByNotificationType(NotificationType.ASSIGNMENT)).thenReturn(List.of(sub1));

        List<Subscription> result = subscriptionService.getSubscriptions(null, NotificationType.ASSIGNMENT);

        assertThat(result).hasSize(1).allMatch(s -> s.getNotificationType() == NotificationType.ASSIGNMENT);
        verify(subscriptionRepository).findByNotificationType(NotificationType.ASSIGNMENT);
    }

    @Test
    void testGetSubscriptionsByUserIdAndType() {
        Subscription sub1 = buildSubscription(1L, "user1", NotificationType.ASSIGNMENT, TargetType.GLOBAL, null, true);
        Subscription sub2 = buildSubscription(2L, "user1", NotificationType.ALERT, TargetType.GLOBAL, null, true);
        when(subscriptionRepository.findByUserId("user1")).thenReturn(List.of(sub1, sub2));

        List<Subscription> result = subscriptionService.getSubscriptions("user1", NotificationType.ASSIGNMENT);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNotificationType()).isEqualTo(NotificationType.ASSIGNMENT);
        verify(subscriptionRepository).findByUserId("user1");
    }

    @Test
    void testGetSubscriptionById() {
        Subscription sub = buildSubscription(10L, "user1", NotificationType.REMINDER, TargetType.GLOBAL, null, true);
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));

        Optional<Subscription> result = subscriptionService.getSubscriptionById(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(10L);
        verify(subscriptionRepository).findById(10L);
    }

    @Test
    void testGetSubscriptionById_NotFound() {
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Subscription> result = subscriptionService.getSubscriptionById(999L);

        assertThat(result).isEmpty();
        verify(subscriptionRepository).findById(999L);
    }

    @Test
    void testCreateSubscription_NewGlobal() {
        SubscriptionCreateDTO dto = new SubscriptionCreateDTO("user1", NotificationType.ASSIGNMENT, TargetType.GLOBAL, null);
        Subscription savedSub = buildSubscription(1L, "user1", NotificationType.ASSIGNMENT, TargetType.GLOBAL, null, true);

        when(subscriptionRepository.findByUserIdAndNotificationTypeAndTargetTypeAndTargetId("user1", NotificationType.ASSIGNMENT, TargetType.GLOBAL, null))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(savedSub);

        Subscription result = subscriptionService.createSubscription(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTargetType()).isEqualTo(TargetType.GLOBAL);
        assertThat(result.getActive()).isTrue();
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testCreateSubscription_NewTargeted() {
        SubscriptionCreateDTO dto = new SubscriptionCreateDTO("user1", NotificationType.RESULT, TargetType.COMPETITION, 42L);
        Subscription savedSub = buildSubscription(2L, "user1", NotificationType.RESULT, TargetType.COMPETITION, 42L, true);

        when(subscriptionRepository.findByUserIdAndNotificationTypeAndTargetTypeAndTargetId("user1", NotificationType.RESULT, TargetType.COMPETITION, 42L))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(savedSub);

        Subscription result = subscriptionService.createSubscription(dto);

        assertThat(result.getTargetType()).isEqualTo(TargetType.COMPETITION);
        assertThat(result.getTargetId()).isEqualTo(42L);
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testCreateSubscription_ReactivateExisting() {
        SubscriptionCreateDTO dto = new SubscriptionCreateDTO("user2", NotificationType.ALERT, TargetType.GLOBAL, null);
        Subscription existingSub = buildSubscription(5L, "user2", NotificationType.ALERT, TargetType.GLOBAL, null, false);
        Subscription reactivatedSub = buildSubscription(5L, "user2", NotificationType.ALERT, TargetType.GLOBAL, null, true);

        when(subscriptionRepository.findByUserIdAndNotificationTypeAndTargetTypeAndTargetId("user2", NotificationType.ALERT, TargetType.GLOBAL, null))
                .thenReturn(Optional.of(existingSub));
        when(subscriptionRepository.save(existingSub)).thenReturn(reactivatedSub);

        Subscription result = subscriptionService.createSubscription(dto);

        assertThat(result.getActive()).isTrue();
        verify(subscriptionRepository).save(existingSub);
    }

    @Test
    void testCreateSubscription_AlreadyActiveReturnsWithoutSave() {
        SubscriptionCreateDTO dto = new SubscriptionCreateDTO("user3", NotificationType.REMINDER, TargetType.GLOBAL, null);
        Subscription existingSub = buildSubscription(10L, "user3", NotificationType.REMINDER, TargetType.GLOBAL, null, true);

        when(subscriptionRepository.findByUserIdAndNotificationTypeAndTargetTypeAndTargetId("user3", NotificationType.REMINDER, TargetType.GLOBAL, null))
                .thenReturn(Optional.of(existingSub));

        Subscription result = subscriptionService.createSubscription(dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getActive()).isTrue();
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void testCreateSubscription_GlobalWithTargetId_shouldThrow() {
        SubscriptionCreateDTO dto = new SubscriptionCreateDTO("user1", NotificationType.ALERT, TargetType.GLOBAL, 42L);

        assertThatThrownBy(() -> subscriptionService.createSubscription(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("targetId must be null when targetType is GLOBAL");
    }

    @Test
    void testCreateSubscription_CompetitionWithoutTargetId_shouldThrow() {
        SubscriptionCreateDTO dto = new SubscriptionCreateDTO("user1", NotificationType.ALERT, TargetType.COMPETITION, null);

        assertThatThrownBy(() -> subscriptionService.createSubscription(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("targetId is required when targetType is COMPETITION");
    }

    @Test
    void testToggleSubscription_ActivateToInactive() {
        Subscription sub = buildSubscription(20L, "user1", NotificationType.RESULT, TargetType.GLOBAL, null, true);
        when(subscriptionRepository.findById(20L)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArguments()[0]);

        Optional<Subscription> result = subscriptionService.toggleSubscription(20L);

        assertThat(result).isPresent();
        assertThat(result.get().getActive()).isFalse();
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testToggleSubscription_InactiveToActive() {
        Subscription sub = buildSubscription(21L, "user2", NotificationType.CANCELLATION, TargetType.GLOBAL, null, false);
        when(subscriptionRepository.findById(21L)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArguments()[0]);

        Optional<Subscription> result = subscriptionService.toggleSubscription(21L);

        assertThat(result).isPresent();
        assertThat(result.get().getActive()).isTrue();
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testToggleSubscription_NotFound() {
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Subscription> result = subscriptionService.toggleSubscription(999L);

        assertThat(result).isEmpty();
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void testDeleteSubscription() {
        when(subscriptionRepository.existsById(30L)).thenReturn(true);
        doNothing().when(subscriptionRepository).deleteById(30L);

        subscriptionService.deleteSubscription(30L);

        verify(subscriptionRepository).existsById(30L);
        verify(subscriptionRepository).deleteById(30L);
    }

    @Test
    void testDeleteSubscription_NotFound() {
        when(subscriptionRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> subscriptionService.deleteSubscription(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Subscription non trouvée avec l'id: 999");

        verify(subscriptionRepository, never()).deleteById(anyLong());
    }
}
