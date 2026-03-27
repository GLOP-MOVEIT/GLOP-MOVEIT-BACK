package com.moveit.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "ticket", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ticket_validation_token", columnNames = "validation_token")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String ticketNumber;
    private String seatInformation;
    private Instant eventDate;

    @Column(name = "validation_token", nullable = false, unique = true, length = 64)
    private String validationToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
