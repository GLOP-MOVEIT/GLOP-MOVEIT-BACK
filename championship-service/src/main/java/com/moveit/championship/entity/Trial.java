package com.moveit.championship.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "trialId")
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

    /**
     * Numéro du tour (round) dans l'arbre de compétition.
     * Ex : 1 = premier tour, 2 = quarts, 3 = demis, 4 = finale.
     */
    @Column
    private Integer roundNumber;

    /**
     * Position du match dans son tour (1, 2, 3…).
     * Permet de trier les matchs au sein d'un même tour.
     */
    @Column
    private Integer position;

    /**
     * Référence vers le trial suivant dans l'arbre.
     * Le vainqueur de ce trial avance vers le nextTrial.
     * null si c'est la finale (pas de suite).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_trial_id")
    private Trial nextTrial;

}
