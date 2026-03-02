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

class SingleEliminationStrategyTest {

    private SingleEliminationStrategy strategy;
    private Competition competition;

    @BeforeEach
    void setUp() {
        strategy = new SingleEliminationStrategy();
        competition = new Competition();
        competition.setCompetitionId(1);
        competition.setCompetitionName("Tournoi Test");
        competition.setCompetitionSport("Football");
        competition.setCompetitionStatus(Status.PLANNED);
        competition.setCompetitionType(CompetitionType.SINGLE_ELIMINATION);
        competition.setNbManches(0);

        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.MARCH, 1);
        competition.setCompetitionStartDate(cal.getTime());
        cal.set(2026, Calendar.MARCH, 15);
        competition.setCompetitionEndDate(cal.getTime());
    }

    @Test
    @DisplayName("Should generate correct number of trials for 3 rounds (8 participants)")
    void testGenerateTrials_3Rounds() {
        competition.setNbManches(3);
        List<Trial> trials = strategy.generateTrials(competition, List.of());
        // 3 rounds: 4 quarts + 2 demis + 1 finale = 7 matchs
        assertThat(trials).hasSize(7);
    }

    @Test
    @DisplayName("Should generate correct number of trials for 2 rounds (4 participants)")
    void testGenerateTrials_2Rounds() {
        competition.setNbManches(2);
        List<Trial> trials = strategy.generateTrials(competition, List.of());
        // 2 rounds: 2 demis + 1 finale = 3 matchs
        assertThat(trials).hasSize(3);
    }

    @Test
    @DisplayName("Should generate correct number of trials for 1 round (2 participants)")
    void testGenerateTrials_1Round() {
        competition.setNbManches(1);
        List<Trial> trials = strategy.generateTrials(competition, List.of());
        // 1 round: 1 finale
        assertThat(trials).hasSize(1);
        assertThat(trials.getFirst().getTrialName()).contains("Finale");
    }

    @Test
    @DisplayName("Should name rounds correctly")
    void testGenerateTrials_RoundNames() {
        competition.setNbManches(3);
        List<Trial> trials = strategy.generateTrials(competition, List.of());
        assertThat(trials.stream().filter(t -> t.getTrialName().contains("Quarts")).count()).isEqualTo(4);
        assertThat(trials.stream().filter(t -> t.getTrialName().contains("Demi")).count()).isEqualTo(2);
        assertThat(trials.stream().filter(t -> t.getTrialName().contains("Finale")).count()).isEqualTo(1);
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

        // Tour 1 : 4 matchs (positions 1-4)
        List<Trial> round1 = trials.stream().filter(t -> t.getRoundNumber() == 1).toList();
        assertThat(round1).hasSize(4);
        assertThat(round1).extracting(Trial::getPosition).containsExactly(1, 2, 3, 4);

        // Tour 2 : 2 matchs (positions 1-2)
        List<Trial> round2 = trials.stream().filter(t -> t.getRoundNumber() == 2).toList();
        assertThat(round2).hasSize(2);
        assertThat(round2).extracting(Trial::getPosition).containsExactly(1, 2);

        // Tour 3 : 1 match (finale)
        List<Trial> round3 = trials.stream().filter(t -> t.getRoundNumber() == 3).toList();
        assertThat(round3).hasSize(1);
        assertThat(round3).extracting(Trial::getPosition).containsExactly(1);
    }

    @Test
    @DisplayName("Should link trials to next round correctly")
    void testGenerateTrials_NextTrialLinks() {
        competition.setNbManches(3);
        List<Trial> trials = strategy.generateTrials(competition, List.of());

        List<Trial> round1 = trials.stream().filter(t -> t.getRoundNumber() == 1).toList();
        List<Trial> round2 = trials.stream().filter(t -> t.getRoundNumber() == 2).toList();
        List<Trial> round3 = trials.stream().filter(t -> t.getRoundNumber() == 3).toList();

        // Matchs 1 et 2 du tour 1 → match 1 du tour 2
        assertThat(round1.get(0).getNextTrial()).isEqualTo(round2.get(0));
        assertThat(round1.get(1).getNextTrial()).isEqualTo(round2.get(0));

        // Matchs 3 et 4 du tour 1 → match 2 du tour 2
        assertThat(round1.get(2).getNextTrial()).isEqualTo(round2.get(1));
        assertThat(round1.get(3).getNextTrial()).isEqualTo(round2.get(1));

        // Matchs 1 et 2 du tour 2 → finale
        assertThat(round2.get(0).getNextTrial()).isEqualTo(round3.get(0));
        assertThat(round2.get(1).getNextTrial()).isEqualTo(round3.get(0));

        // Finale n'a pas de nextTrial
        assertThat(round3.get(0).getNextTrial()).isNull();
    }

    // --- assignParticipantsToFirstRound tests (tested indirectly via generateTrials) ---

    @Test
    @DisplayName("Should assign 2 participants per match in the first round")
    void testAssignParticipantsToFirstRound_BasicAssignment() {
        competition.setNbManches(2); // 2 demis + 1 finale => 2 matchs au premier tour
        List<Integer> participantIds = List.of(10, 20, 30, 40);

        List<Trial> trials = strategy.generateTrials(competition, participantIds);

        List<Trial> round1 = trials.stream().filter(t -> t.getRoundNumber() == 1).toList();
        assertThat(round1).hasSize(2);
        assertThat(round1.get(0).getParticipantIds()).containsExactly(10, 20);
        assertThat(round1.get(1).getParticipantIds()).containsExactly(30, 40);
    }

    @Test
    @DisplayName("Should assign participants only to first round, not to subsequent rounds")
    void testAssignParticipantsToFirstRound_OnlyFirstRound() {
        competition.setNbManches(2);
        List<Integer> participantIds = List.of(10, 20, 30, 40);

        List<Trial> trials = strategy.generateTrials(competition, participantIds);

        List<Trial> round2 = trials.stream().filter(t -> t.getRoundNumber() == 2).toList();
        assertThat(round2).hasSize(1);
        assertThat(round2.get(0).getParticipantIds()).isEmpty();
    }

    @Test
    @DisplayName("Should handle fewer participants than slots (odd number)")
    void testAssignParticipantsToFirstRound_FewerParticipants() {
        competition.setNbManches(2); // 2 matchs au premier tour = 4 slots
        List<Integer> participantIds = List.of(10, 20, 30); // 3 participants

        List<Trial> trials = strategy.generateTrials(competition, participantIds);

        List<Trial> round1 = trials.stream().filter(t -> t.getRoundNumber() == 1).toList();
        assertThat(round1.get(0).getParticipantIds()).containsExactly(10, 20);
        assertThat(round1.get(1).getParticipantIds()).containsExactly(30); // 1 seul participant (bye)
    }

    @Test
    @DisplayName("Should handle empty participant list without errors")
    void testAssignParticipantsToFirstRound_EmptyList() {
        competition.setNbManches(2);
        List<Integer> participantIds = List.of();

        List<Trial> trials = strategy.generateTrials(competition, participantIds);

        List<Trial> round1 = trials.stream().filter(t -> t.getRoundNumber() == 1).toList();
        assertThat(round1).hasSize(2);
        assertThat(round1.get(0).getParticipantIds()).isEmpty();
        assertThat(round1.get(1).getParticipantIds()).isEmpty();
    }

    @Test
    @DisplayName("Should assign 8 participants across 4 first-round matches")
    void testAssignParticipantsToFirstRound_8Participants3Rounds() {
        competition.setNbManches(3); // 4 quarts + 2 demis + 1 finale
        List<Integer> participantIds = List.of(1, 2, 3, 4, 5, 6, 7, 8);

        List<Trial> trials = strategy.generateTrials(competition, participantIds);

        List<Trial> round1 = trials.stream().filter(t -> t.getRoundNumber() == 1).toList();
        assertThat(round1).hasSize(4);
        assertThat(round1.get(0).getParticipantIds()).containsExactly(1, 2);
        assertThat(round1.get(1).getParticipantIds()).containsExactly(3, 4);
        assertThat(round1.get(2).getParticipantIds()).containsExactly(5, 6);
        assertThat(round1.get(3).getParticipantIds()).containsExactly(7, 8);
    }
}
