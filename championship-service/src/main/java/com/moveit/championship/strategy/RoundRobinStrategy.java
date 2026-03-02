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
public class RoundRobinStrategy implements TreeGenerationStrategy {

    @Override
    public CompetitionType getType() {
        return CompetitionType.ROUND_ROBIN;
    }

    @Override
    public List<Trial> generateTrials(Competition competition, List<Integer> participantIds) {
        List<Integer> ids = new ArrayList<>(participantIds != null ? participantIds : List.of());
        if (ids.size() < 2) {
            throw new IllegalArgumentException("Il faut au moins 2 participants pour un round robin");
        }

        // Si nombre impair, ajouter un "bye" (-1) pour équilibrer
        if (ids.size() % 2 != 0) {
            ids.add(-1);
        }

        int nbParticipants = ids.size();
        int nbRounds = nbParticipants - 1;
        int matchesPerRound = nbParticipants / 2;

        List<Trial> trials = new ArrayList<>();

        // Préparer la liste pour le round-robin (algorithme du cercle)
        List<Integer> rotating = new ArrayList<>(ids.subList(1, ids.size()));

        long totalDuration = competition.getCompetitionEndDate().getTime() - competition.getCompetitionStartDate().getTime();
        long roundDuration = totalDuration / nbRounds;

        int matchNumber = 1;

        for (int round = 1; round <= nbRounds; round++) {
            Date roundStart = new Date(competition.getCompetitionStartDate().getTime() + (round - 1) * roundDuration);
            Date roundEnd = new Date(competition.getCompetitionStartDate().getTime() + round * roundDuration);

            List<Integer> currentOrder = new ArrayList<>();
            currentOrder.add(ids.getFirst());
            currentOrder.addAll(rotating);

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

                int home = currentOrder.get(match - 1);
                int away = currentOrder.get(currentOrder.size() - match);
                List<Integer> matchParticipants = new ArrayList<>();
                if (home != -1) matchParticipants.add(home);
                if (away != -1) matchParticipants.add(away);
                trial.setParticipantIds(matchParticipants);

                trials.add(trial);
                matchNumber++;
            }

            rotating.addFirst(rotating.removeLast());
        }

        return trials;
    }
}
