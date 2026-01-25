package com.moveit.localization.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "location_history", indexes = {
    @Index(name = "idx_history_user_id", columnList = "userId"),
    @Index(name = "idx_history_timestamp", columnList = "timestamp"),
    @Index(name = "idx_history_user_time", columnList = "userId,timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;


    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
