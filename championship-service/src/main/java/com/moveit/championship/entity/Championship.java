package com.moveit.championship.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "championship")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Championship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @OneToMany(mappedBy = "championship", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Competition> competitions = new ArrayList<>();

    @NotBlank(message = "Le nom du championnat est obligatoire")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "La description du championnat est obligatoire")
    @Column(nullable = false)
    private String description;

    @NotNull(message = "La date de début est obligatoire")
    @Column(nullable = false)
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private Status status;
}