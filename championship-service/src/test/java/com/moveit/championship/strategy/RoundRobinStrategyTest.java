package com.moveit.championship.strategy;

import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.CompetitionType;
import com.moveit.championship.entity.Status;
import com.moveit.championship.entity.Trial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoundRobinStrategyTest {

    private RoundRobinStrategy strategy;
    private Competition competition;

    @BeforeEach
    void setUp() {
        strategy = new RoundRobinStrategy();
        competition = new Competition();
        competition.setCompetitionId(1);
        competition.setCompetitionName("Tournoi RR Test");
        competition.setCompetitionSport("Football");
        competition.setCompetitionStatus(Status.PLANNED);
        competition.setCompetitionType(CompetitionType.ROUND_ROBIN);
        competition.setNbManches(0);

        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.MARCH, 1);
        competition.setCompetitionStartDate(cal.getTime());
        cal.set(2026, Calendar.JUNE, 1);
        competition.setCompetitionEndDate(cal.getTime());
    }

    @Test
    @DisplayName("Should generate correct number of trials for 3 rounds (4 participants)")
    void testGenerateTrials_3Rounds() {
        competition.setNbManches(3);
        List<Trial> trials = strategy.generateTrials(competition);
        // 3 rounds, 4 participants: 3 journées x 2 matchs = 6 matchs
        assertThat(trials).hasSize(6);
    }

    @Test
    @DisplayName("Should generate correct number of trials for 5 rounds (6 participants)")
    void testGenerateTrials_5Rounds() {
        competition.setNbManches(5);
        List<Trial> trials = strategy.generateTrials(competition);
        // 5 rounds, 6 participants: 5 journées x 3 matchs = 15 matchs
        assertThat(trials).hasSize(15);
    }

    @Test
    @DisplayName("Should generate correct number of trials for 1 round (2 participants)")
    void testGenerateTrials_1Round() {
        competition.setNbManches(1);
        List<Trial> trials = strategy.generateTrials(competition);
        // 1 round, 2 participants: 1 journée x 1 match = 1 match
        assertThat(trials).hasSize(1);
    }

    @Test
    @DisplayName("Should name trials with Journée format")
    void testGenerateTrials_NamingFormat() {
        competition.setNbManches(3);
        List<Trial> trials = strategy.generateTrials(competition);
        assertThat(trials.getFirst().getTrialName()).contains("Journée 1");
    }

    @Test
    @DisplayName("Should throw when less than 1 round")
    void testGenerateTrials_LessThan1Round() {
        competition.setNbManches(0);
        assertThatThrownBy(() -> strategy.generateTrials(competition))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("All trials should have PLANNED status")
    void testGenerateTrials_AllPlanned() {
        competition.setNbManches(3);
        List<Trial> trials = strategy.generateTrials(competition);
        assertThat(trials).isNotEmpty().allMatch(t -> t.getTrialStatus() == Status.PLANNED);
    }

    @Test
    @DisplayName("All trials should be linked to the competition")
    void testGenerateTrials_AllLinkedToCompetition() {
        competition.setNbManches(3);
        List<Trial> trials = strategy.generateTrials(competition);
        assertThat(trials).isNotEmpty().allMatch(t -> t.getCompetition().equals(competition));
    }

    @Test
    @DisplayName("All trials should have roundNumber and position set")
    void testGenerateTrials_RoundNumberAndPosition() {
        competition.setNbManches(3);
        List<Trial> trials = strategy.generateTrials(competition);
        assertThat(trials).isNotEmpty().allMatch(t -> t.getRoundNumber() != null && t.getPosition() != null);

        // Journée 1 : 2 matchs
        List<Trial> round1 = trials.stream().filter(t -> t.getRoundNumber() == 1).toList();
        assertThat(round1).hasSize(2);
        assertThat(round1).extracting(Trial::getPosition).containsExactly(1, 2);

        // Journée 2 : 2 matchs
        List<Trial> round2 = trials.stream().filter(t -> t.getRoundNumber() == 2).toList();
        assertThat(round2).hasSize(2);

        // Journée 3 : 2 matchs
        List<Trial> round3 = trials.stream().filter(t -> t.getRoundNumber() == 3).toList();
        assertThat(round3).hasSize(2);
    }

    @Test
    @DisplayName("Round robin trials should not have nextTrial links")
    void testGenerateTrials_NoNextTrialLinks() {
        competition.setNbManches(3);
        List<Trial> trials = strategy.generateTrials(competition);
        assertThat(trials).isNotEmpty().allMatch(t -> t.getNextTrial() == null);
    }
}
