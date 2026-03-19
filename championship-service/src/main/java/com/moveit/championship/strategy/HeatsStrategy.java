package com.moveit.championship.strategy;

import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.CompetitionType;
import com.moveit.championship.entity.Status;
import com.moveit.championship.entity.Trial;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class HeatsStrategy implements TreeGenerationStrategy {

    @Override
    public CompetitionType getType() {
        return CompetitionType.HEATS;
    }

    @Override
    public List<Trial> generateTrials(Competition competition, List<Integer> participantIds) {
        int nbManches  = competition.getNbManches();
        int maxPerHeat = competition.getMaxPerHeat();

        validateInputs(nbManches, participantIds.size(), maxPerHeat);
        validateNoDuplicates(participantIds);

        Duration roundDuration = computeRoundDuration(competition, nbManches);
        List<Trial> trials = new ArrayList<>();
        List<Trial> previousRoundTrials = new ArrayList<>();

        for (int round = 1; round <= nbManches; round++) {
            long roundOffset = (long) round - 1L;
            LocalDateTime roundStart = competition.getCompetitionStartDate().plus(roundDuration.multipliedBy(roundOffset));
            LocalDateTime roundEnd   = competition.getCompetitionStartDate().plus(roundDuration.multipliedBy((long) round));

            List<Trial> currentRoundTrials = buildRound(
                    competition, participantIds, round, nbManches, maxPerHeat, roundStart, roundEnd);

            linkRounds(previousRoundTrials, currentRoundTrials);

            trials.addAll(currentRoundTrials);
            previousRoundTrials = currentRoundTrials;
        }

        return trials;
    }

    // --- Validation ---

    private void validateInputs(int nbManches, int nbParticipants, int maxPerHeat) {
        if (nbManches < 1) {
            throw new IllegalArgumentException("Il faut au moins 1 manche pour des séries");
        }
        if (nbParticipants < 2) {
            throw new IllegalArgumentException("Il faut au moins 2 participants pour des séries");
        }
        if (maxPerHeat < 2) {
            throw new IllegalArgumentException("Il faut au moins 2 places par série");
        }
        int minRequired = maxPerHeat * (int) Math.pow(2d, (double) nbManches - 1d);
        if (nbParticipants != minRequired ) {
            throw new IllegalArgumentException(
                    "Avec " + nbManches + " tour(s) et " + maxPerHeat + " place(s) par série, "
                    + "le nombre de participants doit être égal à " + minRequired + " (reçu : " + nbParticipants + ")");
        }
    }

    private void validateNoDuplicates(List<Integer> participantIds) {
        if (participantIds.stream().distinct().count() != participantIds.size()) {
            throw new IllegalArgumentException("La liste des participants contient des doublons");
        }
    }

    // --- Calcul de durée ---

    private Duration computeRoundDuration(Competition competition, int nbManches) {
        Duration totalDuration = Duration.between(
                competition.getCompetitionStartDate(), competition.getCompetitionEndDate());
        return totalDuration.dividedBy(nbManches);
    }

    // --- Construction des séries d'un tour ---

    private List<Trial> buildRound(Competition competition, List<Integer> participantIds,
                                   int round, int nbManches, int maxPerHeat,
                                   LocalDateTime roundStart, LocalDateTime roundEnd) {
        int currentParticipants = computeCurrentParticipants(
                participantIds.size(), round, nbManches, maxPerHeat);
        int nbHeats = (int) Math.ceil((double) currentParticipants / maxPerHeat);

        List<Trial> roundTrials = new ArrayList<>();
        for (int heat = 1; heat <= nbHeats; heat++) {
            int participantsInHeat = Math.min(maxPerHeat, currentParticipants - (heat - 1) * maxPerHeat);
            Trial trial = buildHeatTrial(competition, round, nbManches, heat, nbHeats,
                    participantsInHeat, roundStart, roundEnd);
            if (round == 1) {
                assignFirstRoundParticipants(trial, heat, maxPerHeat, participantsInHeat, participantIds);
            }
            roundTrials.add(trial);
        }
        return roundTrials;
    }

    private int computeCurrentParticipants(int nbParticipants, int round, int nbManches, int maxPerHeat) {
        int expected = maxPerHeat * (int) Math.pow(2d, (double) nbManches - (double) round);
        return (round == 1) ? Math.min(nbParticipants, expected) : expected;
    }

    private Trial buildHeatTrial(Competition competition, int round, int nbManches,
                                  int heat, int nbHeats, int participantsInHeat,
                                  LocalDateTime roundStart, LocalDateTime roundEnd) {
        String roundName = getRoundName(round, nbManches);
        Trial trial = new Trial();
        trial.setCompetition(competition);
        trial.setTrialName(roundName );
        trial.setTrialStartDate(roundStart);
        trial.setTrialEndDate(roundEnd);
        trial.setTrialDescription(roundName + " - Série " + heat + "/" + nbHeats
                + " (" + participantsInHeat + " participants)");
        trial.setTrialStatus(Status.PLANNED);
        trial.setRoundNumber(round);
        trial.setPosition(heat);
        return trial;
    }

    // --- Distribution des participants au 1er tour ---

    private void assignFirstRoundParticipants(Trial trial, int heat, int maxPerHeat,
                                               int participantsInHeat, List<Integer> participantIds) {
        int startIndex = (heat - 1) * maxPerHeat;
        int endIndex = Math.min(startIndex + participantsInHeat, participantIds.size());
        trial.setParticipantIds(new ArrayList<>(participantIds.subList(startIndex, endIndex)));
    }

    // --- Liaison des tours (previousRound → currentRound) ---

    private void linkRounds(List<Trial> previousRoundTrials, List<Trial> currentRoundTrials) {
        if (!previousRoundTrials.isEmpty() && !currentRoundTrials.isEmpty()) {
            for (Trial prevTrial : previousRoundTrials) {
                prevTrial.setNextTrial(currentRoundTrials.getFirst());
            }
        }
    }

    // --- Nom du tour ---

    private String getRoundName(int round, int totalRounds) {
        if (round == totalRounds) {
            return "Finale";
        } else if (round == totalRounds - 1) {
            return "Demi-finales";
        } else {
            return "Séries " + round;
        }
    }
}
