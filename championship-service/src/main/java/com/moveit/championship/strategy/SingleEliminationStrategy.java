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
public class SingleEliminationStrategy implements TreeGenerationStrategy {

    private record EliminationRound(int round, int nbRounds, int matchesInRound,
                                    LocalDateTime roundStart, LocalDateTime roundEnd,
                                    String roundName) {
    }

    @Override
    public CompetitionType getType() {
        return CompetitionType.SINGLE_ELIMINATION;
    }

    @Override
    public List<Trial> generateTrials(Competition competition, List<Integer> participantIds) {
        int nbRounds = competition.getNbManches();
        validateInputs(nbRounds, participantIds);

        List<Trial> trials = new ArrayList<>();
        int matchesInRound = (int) Math.pow(2d, nbRounds - 1d);

        Duration roundDuration = computeRoundDuration(competition, nbRounds);
        List<Trial> previousRoundTrials = new ArrayList<>();

        for (int round = 1; round <= nbRounds; round++) {
            long roundOffset = round - 1L;
            LocalDateTime roundStart = competition.getCompetitionStartDate().plus(roundDuration.multipliedBy(roundOffset));
            LocalDateTime roundEnd   = competition.getCompetitionStartDate().plus(roundDuration.multipliedBy(round));
            EliminationRound eliminationRound = new EliminationRound(
                round, nbRounds, matchesInRound, roundStart, roundEnd, getRoundName(round, nbRounds));

            List<Trial> currentRoundTrials = buildRoundTrials(competition, participantIds, eliminationRound);

            linkPreviousRound(previousRoundTrials, currentRoundTrials);

            trials.addAll(currentRoundTrials);
            previousRoundTrials = currentRoundTrials;
            matchesInRound /= 2;
        }

        return trials;
    }

    // --- Validation ---

    private void validateInputs(int nbRounds, List<Integer> participantIds) {
        if (nbRounds < 1) {
            throw new IllegalArgumentException("Il faut au moins 1 manche pour une élimination directe");
        }

        int nbParticipants = (participantIds == null) ? 0 : participantIds.size();
        int requiredParticipants = (int) Math.pow(2, nbRounds);

        if (nbParticipants < 2) {
            throw new IllegalArgumentException("Il faut au moins 2 participants pour une élimination directe");
        }
        if (nbParticipants != requiredParticipants) {
            throw new IllegalArgumentException(
                    "L'élimination directe à " + nbRounds + " tour(s) requiert exactement "
                    + requiredParticipants + " participants (reçu : " + nbParticipants + ")");
        }
        if (participantIds.stream().distinct().count() != nbParticipants) {
            throw new IllegalArgumentException("La liste des participants contient des doublons");
        }
    }

    // --- Calcul de durée ---

    private Duration computeRoundDuration(Competition competition, int nbRounds) {
        Duration totalDuration = Duration.between(
                competition.getCompetitionStartDate(), competition.getCompetitionEndDate());
        return totalDuration.dividedBy(nbRounds);
    }

    // --- Construction des épreuves d'un tour ---

    private List<Trial> buildRoundTrials(Competition competition, List<Integer> participantIds,
                                         EliminationRound eliminationRound) {
        List<Trial> roundTrials = new ArrayList<>();

        for (int match = 1; match <= eliminationRound.matchesInRound(); match++) {
            Trial trial = buildTrial(competition, eliminationRound, match);
            if (eliminationRound.round() == 1) {
                assignParticipantsToFirstRound(trial, match, participantIds);
            }
            roundTrials.add(trial);
        }
        return roundTrials;
    }

    private Trial buildTrial(Competition competition, EliminationRound eliminationRound, int match) {
        Trial trial = new Trial();
        trial.setCompetition(competition);
        trial.setTrialName(eliminationRound.roundName() + " - Match " + match);
        trial.setTrialStartDate(eliminationRound.roundStart());
        trial.setTrialEndDate(eliminationRound.roundEnd());
        trial.setTrialDescription("Tour " + eliminationRound.round() + "/" + eliminationRound.nbRounds()
                + " - Match " + match + "/" + eliminationRound.matchesInRound());
        trial.setTrialStatus(Status.PLANNED);
        trial.setRoundNumber(eliminationRound.round());
        trial.setPosition(match);
        return trial;
    }

    // --- Liaison des tours (previousRound → currentRound) ---

    private void linkPreviousRound(List<Trial> previousRoundTrials, List<Trial> currentRoundTrials) {
        for (int i = 0; i < previousRoundTrials.size(); i++) {
            previousRoundTrials.get(i).setNextTrial(currentRoundTrials.get(i / 2));
        }
    }

    // --- Distribution des participants au 1er tour ---

    private void assignParticipantsToFirstRound(Trial trial, int match, List<Integer> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            return;
        }
        int startIndex = (match - 1) * 2;
        int endIndex = Math.min(startIndex + 2, participantIds.size());
        if (startIndex < participantIds.size()) {
            trial.setParticipantIds(new ArrayList<>(participantIds.subList(startIndex, endIndex)));
        }
    }

    // --- Nom du tour ---

    private String getRoundName(int round, int totalRounds) {
        if (round == totalRounds) {
            return "Finale";
        } else if (round == totalRounds - 1) {
            return "Demi-finales";
        } else if (round == totalRounds - 2) {
            return "Quarts de finale";
        } else {
            return "Tour " + round;
        }
    }
}
