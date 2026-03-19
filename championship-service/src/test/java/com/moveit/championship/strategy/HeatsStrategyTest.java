package com.moveit.championship.strategy;

import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.CompetitionType;
import com.moveit.championship.entity.Status;
import com.moveit.championship.entity.Trial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeatsStrategyTest {

    private HeatsStrategy strategy;
    private Competition competition;
    private List<Integer> participantIds;

    private static List<Integer> createParticipantIds(int count) {
        return IntStream.rangeClosed(1, count).boxed().toList();
    }

    @BeforeEach
    void setUp() {
        strategy = new HeatsStrategy();
        competition = new Competition();
        competition.setCompetitionId(1);
        competition.setCompetitionName("100m Nage Libre");
        competition.setCompetitionSport("Natation");
        competition.setCompetitionStatus(Status.PLANNED);
        competition.setCompetitionType(CompetitionType.HEATS);
        competition.setNbManches(3);
        competition.setMaxPerHeat(8);
        // nbManches=3, maxPerHeat=8 → min requis = 8 * 2^(3-1) = 32 participants
        participantIds = createParticipantIds(32);

        competition.setCompetitionStartDate(LocalDateTime.of(2026, 7, 1, 0, 0));
        competition.setCompetitionEndDate(LocalDateTime.of(2026, 7, 3, 0, 0));
    }

    @Test
    @DisplayName("Should generate correct number of trials for 32 participants, 8 per heat, 3 rounds")
    void testGenerateTrials_29Participants_3Rounds() {
        // Finale: 1 × 8 = 8 participants → 1 série
        // Demi-finales: 2 × 8 = 16 participants → 2 séries
        // Séries: 4 × 8 = 32 participants → 4 séries
        // Total: 4 + 2 + 1 = 7 trials
        List<Trial> trials = strategy.generateTrials(competition, participantIds);
        assertThat(trials).hasSize(7);
    }

    @Test
    @DisplayName("Should generate correct round structure for 32 participants")
    void testGenerateTrials_RoundStructure() {
        List<Trial> trials = strategy.generateTrials(competition, participantIds);

        // Round 1 (Séries): 4 × 8 = 32 → 4 séries
        List<Trial> round1 = trials.stream().filter(t -> t.getRoundNumber() == 1).toList();
        assertThat(round1).hasSize(4);

        // Round 2 (Demi-finales): 2 × 8 = 16 → 2 séries
        List<Trial> round2 = trials.stream().filter(t -> t.getRoundNumber() == 2).toList();
        assertThat(round2).hasSize(2);

        // Round 3 (Finale): 1 × 8 = 8 → 1 série
        List<Trial> round3 = trials.stream().filter(t -> t.getRoundNumber() == 3).toList();
        assertThat(round3).hasSize(1);
    }

    @Test
    @DisplayName("Should name rounds correctly")
    void testGenerateTrials_RoundNames() {
        List<Trial> trials = strategy.generateTrials(competition, participantIds);

        List<Trial> round1 = trials.stream().filter(t -> t.getRoundNumber() == 1).toList();
        assertThat(round1).isNotEmpty().allMatch(t -> t.getTrialName().contains("Séries"));

        List<Trial> round2 = trials.stream().filter(t -> t.getRoundNumber() == 2).toList();
        assertThat(round2).isNotEmpty().allMatch(t -> t.getTrialName().contains("Demi-finales"));

        List<Trial> round3 = trials.stream().filter(t -> t.getRoundNumber() == 3).toList();
        assertThat(round3).isNotEmpty().allMatch(t -> t.getTrialName().contains("Finale"));
    }

    @Test
    @DisplayName("Should generate correct trials for 16 participants, 2 rounds")
    void testGenerateTrials_16Participants_2Rounds() {
        List<Integer> ids16 = createParticipantIds(16);
        competition.setNbManches(2);

        List<Trial> trials = strategy.generateTrials(competition, ids16);

        // Round 1: 2 × maxPerHeat = 16, 16 participants → 16/8 = 2 séries
        List<Trial> round1 = trials.stream().filter(t -> t.getRoundNumber() == 1).toList();
        assertThat(round1).hasSize(2);

        // Round 2 (Finale): 1 × maxPerHeat = 8 → 8/8 = 1
        List<Trial> round2 = trials.stream().filter(t -> t.getRoundNumber() == 2).toList();
        assertThat(round2).hasSize(1);

        assertThat(trials).hasSize(3);
    }

    @Test
    @DisplayName("Should generate single finale for 8 or fewer participants with 1 round")
    void testGenerateTrials_8Participants_1Round() {
        List<Integer> ids8 = createParticipantIds(8);
        competition.setNbManches(1);

        List<Trial> trials = strategy.generateTrials(competition, ids8);
        assertThat(trials).hasSize(1);
        assertThat(trials.getFirst().getTrialName()).isEqualTo("Finale");
    }

    @Test
    @DisplayName("Should include participant count in description")
    void testGenerateTrials_DescriptionContainsParticipants() {
        List<Trial> trials = strategy.generateTrials(competition, participantIds);
        // Chaque série du round 1 a 8 participants (32 / 4 séries = 8)
        assertThat(trials.getFirst().getTrialDescription()).contains("8 participants");
        // Dernière série du round 1 a aussi 8 participants (32 est divisible par 8)
        List<Trial> round1 = trials.stream().filter(t -> t.getRoundNumber() == 1).toList();
        assertThat(round1.getLast().getTrialDescription()).contains("8 participants");
    }

    @Test
    @DisplayName("Should throw when participant count is different from required bracket size")
    void testGenerateTrials_InvalidParticipantsCountForBracket() {
        // nbManches=3, maxPerHeat=8 → requis exact = 32, on passe 10
        List<Integer> tooFew = createParticipantIds(10);
        assertThatThrownBy(() -> strategy.generateTrials(competition, tooFew))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("doit être égal à 32");
    }

    @Test
    @DisplayName("Should throw when participant list contains duplicates")
    void testGenerateTrials_DuplicateParticipants() {
        // On recrée une liste de 32 avec un doublon (on remplace le dernier par 1)
        List<Integer> withDuplicate = new java.util.ArrayList<>(createParticipantIds(31));
        withDuplicate.add(1); // doublon de 1
        assertThatThrownBy(() -> strategy.generateTrials(competition, withDuplicate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("doublons");
    }

    @Test
    @DisplayName("Should throw when less than 1 round")
    void testGenerateTrials_LessThan1Round() {
        competition.setNbManches(0);
        assertThatThrownBy(() -> strategy.generateTrials(competition, participantIds))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw when less than 2 participants")
    void testGenerateTrials_LessThan2Participants() {
        List<Integer> ids1 = createParticipantIds(1);
        assertThatThrownBy(() -> strategy.generateTrials(competition, ids1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw when less than 2 per heat")
    void testGenerateTrials_LessThan2PerHeat() {
        competition.setMaxPerHeat(1);
        assertThatThrownBy(() -> strategy.generateTrials(competition, participantIds))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("All trials should have PLANNED status")
    void testGenerateTrials_AllPlanned() {
        List<Trial> trials = strategy.generateTrials(competition, participantIds);
        assertThat(trials).isNotEmpty().allMatch(t -> t.getTrialStatus() == Status.PLANNED);
    }

    @Test
    @DisplayName("All trials should be linked to the competition")
    void testGenerateTrials_AllLinkedToCompetition() {
        List<Trial> trials = strategy.generateTrials(competition, participantIds);
        assertThat(trials).isNotEmpty().allMatch(t -> t.getCompetition().equals(competition));
    }

    @Test
    @DisplayName("All trials should have roundNumber and position set")
    void testGenerateTrials_RoundNumberAndPosition() {
        List<Trial> trials = strategy.generateTrials(competition, participantIds);
        assertThat(trials).isNotEmpty().allMatch(t -> t.getRoundNumber() != null && t.getPosition() != null);

        // Round 1: positions 1-4
        List<Trial> round1 = trials.stream().filter(t -> t.getRoundNumber() == 1).toList();
        assertThat(round1).extracting(Trial::getPosition).containsExactly(1, 2, 3, 4);
    }

    @Test
    @DisplayName("Should return HEATS type")
    void testGetType() {
        assertThat(strategy.getType()).isEqualTo(CompetitionType.HEATS);
    }
}
