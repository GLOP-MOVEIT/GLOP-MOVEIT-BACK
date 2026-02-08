package com.moveit.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer userId;

    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String surname;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(unique = true, nullable = false)
    private String phoneNumber;
    @Column(nullable = false)
    private String language;
    @Column(nullable = false)
    private boolean acceptsNotifications;
    @Column(nullable = false)
    private boolean acceptsLocationSharing;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role", nullable = false)
    private RoleEntity role;
}