package com.moveit.volunteer_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.moveit.volunteer_service.enums.TaskStatus;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "volunteer_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "championship_id")
    private Long championshipId;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_type_id", nullable = false)
    private VolunteerTaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "max_volunteers")
    private Integer maxVolunteers;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "task_volunteers", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "user_id")
    private Set<Long> assignedVolunteerIds = new HashSet<>();

    @Column(length = 500)
    private String location;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


}
