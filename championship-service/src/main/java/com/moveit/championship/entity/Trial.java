package com.moveit.championship.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Table(name = "trial")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trial_id", nullable = false)
    private Integer trialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    @JsonBackReference(value = "competition-trial")
    private Competition competition;

    @NotBlank(message = "Le nom de la manche est obligatoire")
    @Column(nullable = false)
    private String trialName;

    @NotNull(message = "La date de début est obligatoire")
    @Column(nullable = false)
    private Date trialStartDate;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(nullable = false)
    private Date trialEndDate;

    @Column(length = 1000)
    private String trialDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status trialStatus = Status.PLANNED;

    @Column
    private String location;

}
