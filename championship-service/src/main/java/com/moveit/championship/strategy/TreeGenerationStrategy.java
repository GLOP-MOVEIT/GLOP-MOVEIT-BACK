package com.moveit.championship.strategy;

import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.CompetitionType;
import com.moveit.championship.entity.Trial;

import java.util.List;


public interface TreeGenerationStrategy {


    CompetitionType getType();

    List<Trial> generateTrials(Competition competition);
}
