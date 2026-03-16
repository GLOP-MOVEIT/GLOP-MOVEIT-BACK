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

    @Override
    public CompetitionType getType() {
        return CompetitionType.SINGLE_ELIMINATION;
    }

    @Override
    public List<Trial> generateTrials(Competition competition, List<Integer> participantIds) {
        int nbRounds = competition.getNbManches();
        if (nbRounds < 1) {
            throw new IllegalArgumentException("Il faut au moins 1 manche pour une élimination directe");
        }

        int requiredParticipants = (int) Math.pow(2, nbRounds);
        int nbParticipants = (participantIds == null) ? 0 : participantIds.size();

        if (nbParticipants < 2) {
            throw new IllegalArgumentException(
                    "Il faut au moins 2 participants pour une élimination directe");
        }
        if (nbParticipants != requiredParticipants) {
            throw new IllegalArgumentException(
                    "L'élimination directe à " + nbRounds + " tour(s) requiert exactement "
                    + requiredParticipants + " participants (reçu : " + nbParticipants + ")");
        }
        long distinctCount = participantIds.stream().distinct().count();
        if (distinctCount != nbParticipants) {
            throw new IllegalArgumentException("La liste des participants contient des doublons");
        }

        List<Trial> trials = new ArrayList<>();
        int matchesInRound = (int) Math.pow(2, (double) nbRounds - 1);

        Duration totalDuration = Duration.between(competition.getCompetitionStartDate(), competition.getCompetitionEndDate());
        Duration roundDuration = totalDuration.dividedBy(nbRounds);

        // Map pour retrouver les trials du tour précédent afin de créer les liens
        List<Trial> previousRoundTrials = new ArrayList<>();

        for (int round = 1; round <= nbRounds; round++) {
            String roundName = getRoundName(round, nbRounds);

            LocalDateTime roundStart = competition.getCompetitionStartDate().plus(roundDuration.multipliedBy((long) (round - 1)));
            LocalDateTime roundEnd = competition.getCompetitionStartDate().plus(roundDuration.multipliedBy((long) round));

            List<Trial> currentRoundTrials = new ArrayList<>();

            for (int match = 1; match <= matchesInRound; match++) {
                Trial trial = new Trial();
                trial.setCompetition(competition);
                trial.setTrialName(roundName + " - Match " + match);
                trial.setTrialStartDate(roundStart);
                trial.setTrialEndDate(roundEnd);
                trial.setTrialDescription("Tour " + round + "/" + nbRounds + " - Match " + match + "/" + matchesInRound);
                trial.setTrialStatus(Status.PLANNED);
                trial.setRoundNumber(round);
                trial.setPosition(match);

                // Distribuer 2 participants par match au premier tour
                if (round == 1) {
                    assignParticipantsToFirstRound(trial, match, participantIds);
                }

                currentRoundTrials.add(trial);
                trials.add(trial);
            }

            // Lie les matchs du tour précédent vers ceux du tour actuel
            // 2 matchs consécutifs du tour N alimentent 1 match du tour N+1
            if (!previousRoundTrials.isEmpty()) {
                for (int i = 0; i < previousRoundTrials.size(); i++) {
                    Trial previousTrial = previousRoundTrials.get(i);
                    Trial nextTrial = currentRoundTrials.get(i / 2);
                    previousTrial.setNextTrial(nextTrial);
                }
            }

            previousRoundTrials = currentRoundTrials;
            matchesInRound /= 2;
        }

        return trials;
    }

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
