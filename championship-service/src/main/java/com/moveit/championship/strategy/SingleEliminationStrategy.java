package com.moveit.championship.strategy;

import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.CompetitionType;
import com.moveit.championship.entity.Status;
import com.moveit.championship.entity.Trial;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
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

        List<Trial> trials = new ArrayList<>();
        int matchesInRound = (int) Math.pow(2, nbRounds - 1);

        long totalDuration = competition.getCompetitionEndDate().getTime() - competition.getCompetitionStartDate().getTime();
        long roundDuration = totalDuration / nbRounds;

        // Map pour retrouver les trials du tour précédent afin de créer les liens
        List<Trial> previousRoundTrials = new ArrayList<>();

        for (int round = 1; round <= nbRounds; round++) {
            String roundName = getRoundName(round, nbRounds);

            Date roundStart = new Date(competition.getCompetitionStartDate().getTime() + (round - 1) * roundDuration);
            Date roundEnd = new Date(competition.getCompetitionStartDate().getTime() + round * roundDuration);

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
                if (round == 1 && participantIds != null && !participantIds.isEmpty()) {
                    int startIndex = (match - 1) * 2;
                    int endIndex = Math.min(startIndex + 2, participantIds.size());
                    if (startIndex < participantIds.size()) {
                        trial.setParticipantIds(new ArrayList<>(participantIds.subList(startIndex, endIndex)));
                    }
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
