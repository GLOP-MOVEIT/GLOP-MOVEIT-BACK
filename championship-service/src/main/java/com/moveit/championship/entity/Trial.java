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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Column(name = "location_id")
    private Integer locationId;

    @Column
    private Integer roundNumber;

    @Column
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_trial_id")
    private Trial nextTrial;

    @ElementCollection
    @CollectionTable(name = "trial_participants", joinColumns = @JoinColumn(name = "trial_id"))
    @Column(name = "participant_id")
    private List<Integer> participantIds = new ArrayList<>();

}
