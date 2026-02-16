package com.moveit.championship.strategy;

import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.CompetitionType;
import com.moveit.championship.entity.Status;
import com.moveit.championship.entity.Trial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

    @ParameterizedTest
    @CsvSource({"3, 6", "5, 15", "1, 1"})
    @DisplayName("Should generate correct number of trials for given rounds")
    void testGenerateTrials_CorrectTrialCount(int nbManches, int expectedTrials) {
        competition.setNbManches(nbManches);
        List<Trial> trials = strategy.generateTrials(competition, List.of());
        assertThat(trials).hasSize(expectedTrials);
    }

    @Test
    @DisplayName("Should name trials with Journée format")
    void testGenerateTrials_NamingFormat() {
        competition.setNbManches(3);
        List<Trial> trials = strategy.generateTrials(competition, List.of());
        assertThat(trials.getFirst().getTrialName()).contains("Journée 1");
    }

    @Test
    @DisplayName("Should throw when less than 1 round")
    void testGenerateTrials_LessThan1Round() {
        competition.setNbManches(0);
        List<Integer> participantIds = List.of();
        assertThatThrownBy(() -> strategy.generateTrials(competition, participantIds))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("All trials should have PLANNED status")
    void testGenerateTrials_AllPlanned() {
        competition.setNbManches(3);
        List<Trial> trials = strategy.generateTrials(competition, List.of());
        assertThat(trials).isNotEmpty().allMatch(t -> t.getTrialStatus() == Status.PLANNED);
    }

    @Test
    @DisplayName("All trials should be linked to the competition")
    void testGenerateTrials_AllLinkedToCompetition() {
        competition.setNbManches(3);
        List<Trial> trials = strategy.generateTrials(competition, List.of());
        assertThat(trials).isNotEmpty().allMatch(t -> t.getCompetition().equals(competition));
    }

    @Test
    @DisplayName("All trials should have roundNumber and position set")
    void testGenerateTrials_RoundNumberAndPosition() {
        competition.setNbManches(3);
        List<Trial> trials = strategy.generateTrials(competition, List.of());
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
        List<Trial> trials = strategy.generateTrials(competition, List.of());
        assertThat(trials).isNotEmpty().allMatch(t -> t.getNextTrial() == null);
    }
}
