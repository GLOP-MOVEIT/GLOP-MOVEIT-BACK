package com.moveit.user.entity;

import com.moveit.user.dto.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer requestId;

    private RequestStatus requestStatus;
    private String requestRejectionReason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "request_id", nullable = false)
    private List<DocumentEntity> documents;
}