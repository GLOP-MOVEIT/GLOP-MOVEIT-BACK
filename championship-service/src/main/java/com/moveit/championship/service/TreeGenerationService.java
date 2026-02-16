package com.moveit.championship.service;

import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.Trial;
import com.moveit.championship.exception.CompetitionNotFoundException;
import com.moveit.championship.repository.CompetitionRepository;
import com.moveit.championship.strategy.TreeGenerationStrategy;
import com.moveit.championship.strategy.TreeStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TreeGenerationService {

    private final CompetitionRepository competitionRepository;
    private final TreeStrategyFactory treeStrategyFactory;


    @Transactional
    public Competition generateTree(Integer competitionId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException(competitionId));


        competition.getTrials().clear();
        competitionRepository.save(competition);

        TreeGenerationStrategy strategy = treeStrategyFactory.getStrategy(competition.getCompetitionType());

        List<Trial> generatedTrials = strategy.generateTrials(competition);

        competition.getTrials().addAll(generatedTrials);

        return competitionRepository.save(competition);
    }
}
