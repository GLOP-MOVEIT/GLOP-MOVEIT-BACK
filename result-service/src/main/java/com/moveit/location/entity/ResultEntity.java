package com.moveit.location.entity;

import com.moveit.location.dto.Ranking;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer resultId;

    private Integer trialId;
    private boolean lastTrial;

    @ElementCollection
    @CollectionTable(name = "ranking", joinColumns = @JoinColumn(name = "result_id"))
    private List<Ranking> rankings;
}