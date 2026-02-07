package com.moveit.championship.strategy;

import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.CompetitionType;
import com.moveit.championship.entity.Status;
import com.moveit.championship.entity.Trial;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Stratégie Round Robin (tous contre tous).
 * Chaque participant affronte tous les autres une fois.
 * Ex : 4 participants → 6 matchs (combinaisons C(4,2))
 */
@Component
public class RoundRobinStrategy implements TreeGenerationStrategy {

    @Override
    public CompetitionType getType() {
        return CompetitionType.ROUND_ROBIN;
    }

    @Override
    public List<Trial> generateTrials(Competition competition, int nbParticipants) {
        if (nbParticipants < 2) {
            throw new IllegalArgumentException("Il faut au moins 2 participants pour un round robin");
        }

        List<Trial> trials = new ArrayList<>();
        int nbRounds = nbParticipants - 1;
        int matchesPerRound = nbParticipants / 2;

        long totalDuration = competition.getCompetitionEndDate().getTime() - competition.getCompetitionStartDate().getTime();
        long roundDuration = totalDuration / nbRounds;

        int matchNumber = 1;

        for (int round = 1; round <= nbRounds; round++) {
            Date roundStart = new Date(competition.getCompetitionStartDate().getTime() + (round - 1) * roundDuration);
            Date roundEnd = new Date(competition.getCompetitionStartDate().getTime() + round * roundDuration);

            for (int match = 1; match <= matchesPerRound; match++) {
                Trial trial = new Trial();
                trial.setCompetition(competition);
                trial.setTrialName("Journée " + round + " - Match " + match);
                trial.setTrialStartDate(roundStart);
                trial.setTrialEndDate(roundEnd);
                trial.setTrialDescription("Journée " + round + "/" + nbRounds + " - Match " + match + "/" + matchesPerRound + " (match global n°" + matchNumber + ")");
                trial.setTrialStatus(Status.PLANNED);
                trial.setRoundNumber(round);
                trial.setPosition(match);
                trials.add(trial);
                matchNumber++;
            }
        }

        return trials;
    }
}
