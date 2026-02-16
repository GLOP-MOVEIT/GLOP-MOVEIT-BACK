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
public class HeatsStrategy implements TreeGenerationStrategy {

    @Override
    public CompetitionType getType() {
        return CompetitionType.HEATS;
    }

    @Override
    public List<Trial> generateTrials(Competition competition) {
        int nbManches = competition.getNbManches();
        int nbParticipants = competition.getNbParticipants();
        int maxPerHeat = competition.getMaxPerHeat();

        if (nbManches < 1) {
            throw new IllegalArgumentException("Il faut au moins 1 manche pour des séries");
        }
        if (nbParticipants < 2) {
            throw new IllegalArgumentException("Il faut au moins 2 participants pour des séries");
        }
        if (maxPerHeat < 2) {
            throw new IllegalArgumentException("Il faut au moins 2 places par série");
        }

        List<Trial> trials = new ArrayList<>();

        long totalDuration = competition.getCompetitionEndDate().getTime() - competition.getCompetitionStartDate().getTime();
        long roundDuration = totalDuration / nbManches;

        List<Trial> previousRoundTrials = new ArrayList<>();

        for (int round = 1; round <= nbManches; round++) {
            String roundName = getRoundName(round, nbManches);

            // Nombre de participants attendus pour ce tour : maxPerHeat * 2^(nbManches - round)
            // Finale = 1 * maxPerHeat, Demi = 2 * maxPerHeat, etc.
            int expectedParticipants = maxPerHeat * (int) Math.pow(2, nbManches - round);

            // Pour le premier tour, on cap au nombre réel de participants
            int currentParticipants = (round == 1)
                    ? Math.min(nbParticipants, expectedParticipants)
                    : expectedParticipants;

            int nbHeats = (int) Math.ceil((double) currentParticipants / maxPerHeat);

            Date roundStart = new Date(competition.getCompetitionStartDate().getTime() + (round - 1) * roundDuration);
            Date roundEnd = new Date(competition.getCompetitionStartDate().getTime() + round * roundDuration);

            List<Trial> currentRoundTrials = new ArrayList<>();

            for (int heat = 1; heat <= nbHeats; heat++) {
                int participantsInHeat = Math.min(maxPerHeat, currentParticipants - (heat - 1) * maxPerHeat);

                Trial trial = new Trial();
                trial.setCompetition(competition);
                trial.setTrialName(roundName + " - Série " + heat);
                trial.setTrialStartDate(roundStart);
                trial.setTrialEndDate(roundEnd);
                trial.setTrialDescription(roundName + " - Série " + heat + "/" + nbHeats
                        + " (" + participantsInHeat + " participants)");
                trial.setTrialStatus(Status.PLANNED);
                trial.setRoundNumber(round);
                trial.setPosition(heat);

                currentRoundTrials.add(trial);
                trials.add(trial);
            }

            // Link previous round heats to current round heats
            if (!previousRoundTrials.isEmpty() && !currentRoundTrials.isEmpty()) {
                for (Trial prevTrial : previousRoundTrials) {
                    prevTrial.setNextTrial(currentRoundTrials.getFirst());
                }
            }

            previousRoundTrials = currentRoundTrials;
        }

        return trials;
    }

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
