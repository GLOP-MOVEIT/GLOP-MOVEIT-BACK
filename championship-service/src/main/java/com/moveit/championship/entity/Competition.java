package com.moveit.championship.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(name = "competition")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Competition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "competition_id", nullable = false)
    private Integer competitionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "championship_id", nullable = false)
    @JsonBackReference(value = "championship-competition")
    private Championship championship;

    @NotBlank(message = "Le sport de la compétition est obligatoire")
    @Column(nullable = false)
    private String competitionSport;

    @NotBlank(message = "Le nom de la compétition est obligatoire")
    @Column(nullable = false)
    private String competitionName;

    @NotNull(message = "La date de début est obligatoire")
    @Column(nullable = false)
    private Date competitionStartDate;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(nullable = false)
    private Date competitionEndDate;

    @Column(length = 1000)
    private String competitionDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status competitionStatus = Status.PLANNED;

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "competition-event")
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "competition-trial")
    private List<Trial> trials = new ArrayList<>();

    @NotNull(message = "Le nombre de manches est obligatoire")
    @Column(nullable = false)
    private Integer nbManches;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitionType competitionType = CompetitionType.SINGLE_ELIMINATION;

    @Column(name = "max_per_heat")
    private Integer maxPerHeat;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_type", nullable = false)
    private ParticipantType participantType = ParticipantType.INDIVIDUAL;
}