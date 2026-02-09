package com.moveit.location.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "location")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id", nullable = false)
    private Integer locationId;

    @NotBlank(message = "Le nom du lieu est obligatoire")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "La latitude est obligatoire")
    @Column(nullable = false)
    private Double latitude;

    @NotNull(message = "La longitude est obligatoire")
    @Column(nullable = false)
    private Double longitude;

    @NotBlank(message = "L'entrée principale est obligatoire")
    @Column(nullable = false, length = 500)
    private String mainEntrance;

    @Column(length = 500)
    private String refereeEntrance;

    @Column(length = 500)
    private String athleteEntrance;

    @Column(length = 500)
    private String vipEntrance;

    @Column(length = 1000)
    private String description;
}
