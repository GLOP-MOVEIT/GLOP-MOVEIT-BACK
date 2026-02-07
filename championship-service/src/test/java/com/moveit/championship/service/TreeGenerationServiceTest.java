package com.moveit.championship.service;

import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.CompetitionType;
import com.moveit.championship.entity.Status;
import com.moveit.championship.entity.Trial;
import com.moveit.championship.exception.CompetitionNotFoundException;
import com.moveit.championship.repository.CompetitionRepository;
import com.moveit.championship.repository.TrialRepository;
import com.moveit.championship.strategy.SingleEliminationStrategy;
import com.moveit.championship.strategy.TreeGenerationStrategy;
import com.moveit.championship.strategy.TreeStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TreeGenerationServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;
    @Mock
    private TrialRepository trialRepository;
    @Mock
    private TreeStrategyFactory treeStrategyFactory;
    @InjectMocks
    private TreeGenerationService treeGenerationService;

    private Competition competition;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        competition = new Competition();
        competition.setCompetitionId(1);
        competition.setCompetitionName("Tournoi Test");
        competition.setCompetitionSport("Football");
        competition.setCompetitionStatus(Status.PLANNED);
        competition.setCompetitionType(CompetitionType.SINGLE_ELIMINATION);
        competition.setNbManches(0);
        competition.setTrials(new ArrayList<>());

        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.MARCH, 1);
        competition.setCompetitionStartDate(cal.getTime());
        cal.set(2026, Calendar.MARCH, 15);
        competition.setCompetitionEndDate(cal.getTime());
    }

    @Test
    @DisplayName("Should generate tree and save competition with trials")
    void testGenerateTree_Success() {
        Trial trial1 = new Trial();
        trial1.setTrialName("Finale - Match 1");
        trial1.setTrialStatus(Status.PLANNED);
        trial1.setCompetition(competition);
        trial1.setTrialStartDate(competition.getCompetitionStartDate());
        trial1.setTrialEndDate(competition.getCompetitionEndDate());

        TreeGenerationStrategy mockStrategy = mock(TreeGenerationStrategy.class);
        when(competitionRepository.findById(1)).thenReturn(Optional.of(competition));
        when(treeStrategyFactory.getStrategy(CompetitionType.SINGLE_ELIMINATION)).thenReturn(mockStrategy);
        when(mockStrategy.generateTrials(any(Competition.class), eq(2))).thenReturn(List.of(trial1));
        when(competitionRepository.save(any(Competition.class))).thenAnswer(inv -> inv.getArgument(0));

        Competition result = treeGenerationService.generateTree(1, 2);

        assertThat(result.getTrials()).hasSize(1);
        assertThat(result.getNbManches()).isEqualTo(1);
        verify(competitionRepository, times(2)).save(any(Competition.class));
        verify(treeStrategyFactory).getStrategy(CompetitionType.SINGLE_ELIMINATION);
    }

    @Test
    @DisplayName("Should throw when competition not found")
    void testGenerateTree_CompetitionNotFound() {
        when(competitionRepository.findById(999)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> treeGenerationService.generateTree(999, 8))
                .isInstanceOf(CompetitionNotFoundException.class);
    }

    @Test
    @DisplayName("Should clear existing trials before generating new ones")
    void testGenerateTree_ClearsExistingTrials() {
        Trial existingTrial = new Trial();
        existingTrial.setTrialId(99);
        existingTrial.setTrialName("Old Trial");
        existingTrial.setCompetition(competition);
        competition.getTrials().add(existingTrial);

        TreeGenerationStrategy mockStrategy = mock(TreeGenerationStrategy.class);
        when(competitionRepository.findById(1)).thenReturn(Optional.of(competition));
        when(treeStrategyFactory.getStrategy(CompetitionType.SINGLE_ELIMINATION)).thenReturn(mockStrategy);
        when(mockStrategy.generateTrials(any(Competition.class), eq(2))).thenReturn(List.of());
        when(competitionRepository.save(any(Competition.class))).thenAnswer(inv -> inv.getArgument(0));

        Competition result = treeGenerationService.generateTree(1, 2);

        assertThat(result.getTrials()).isEmpty();
    }
}
