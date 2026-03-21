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
public class RoundRobinStrategy implements TreeGenerationStrategy {

    private record RoundSchedule(int round, int nbRounds, int matchesPerRound, int startMatchNumber,
                                 LocalDateTime roundStart, LocalDateTime roundEnd) {
    }

    @Override
    public CompetitionType getType() {
        return CompetitionType.ROUND_ROBIN;
    }

    @Override
    public List<Trial> generateTrials(Competition competition, List<Integer> participantIds) {
        List<Integer> ids = normalize(participantIds);
        validate(ids);

        int nbRounds       = ids.size() - 1;
        int matchesPerRound = ids.size() / 2;
        Duration roundDuration = computeRoundDuration(competition, nbRounds);

        List<Trial> trials = new ArrayList<>();
        List<Integer> rotating = new ArrayList<>(ids.subList(1, ids.size()));
        int matchNumber = 1;

        for (int round = 1; round <= nbRounds; round++) {
            long roundOffset = round - 1L;
            LocalDateTime roundStart = competition.getCompetitionStartDate().plus(roundDuration.multipliedBy(roundOffset));
            LocalDateTime roundEnd   = competition.getCompetitionStartDate().plus(roundDuration.multipliedBy(round));
            RoundSchedule schedule = new RoundSchedule(
                round, nbRounds, matchesPerRound, matchNumber, roundStart, roundEnd);

            List<Integer> currentOrder = buildCurrentOrder(ids.getFirst(), rotating);
            List<Trial> roundTrials = buildRoundTrials(competition, currentOrder, schedule);

            trials.addAll(roundTrials);
            matchNumber += matchesPerRound;
            rotating.addFirst(rotating.removeLast());
        }

        return trials;
    }

    // --- Normalisation et validation ---

    private List<Integer> normalize(List<Integer> participantIds) {
        return new ArrayList<>(participantIds != null ? participantIds : List.of());
    }

    private void validate(List<Integer> ids) {
        if (ids.size() < 2) {
            throw new IllegalArgumentException("Il faut au moins 2 participants pour un round robin");
        }
        if (ids.size() % 2 != 0) {
            throw new IllegalArgumentException(
                    "Le round robin requiert un nombre pair de participants (reçu : " + ids.size() + ")");
        }
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalArgumentException("La liste des participants contient des doublons");
        }
    }


    // --- Calcul de durée ---

    private Duration computeRoundDuration(Competition competition, int nbRounds) {
        Duration totalDuration = Duration.between(
                competition.getCompetitionStartDate(), competition.getCompetitionEndDate());
        return totalDuration.dividedBy(nbRounds);
    }

    // --- Construction de l'ordre du tour courant ---

    private List<Integer> buildCurrentOrder(int fixed, List<Integer> rotating) {
        List<Integer> order = new ArrayList<>();
        order.add(fixed);
        order.addAll(rotating);
        return order;
    }

    // --- Construction des épreuves d'une journée ---

    private List<Trial> buildRoundTrials(Competition competition, List<Integer> currentOrder, RoundSchedule schedule) {
        List<Trial> roundTrials = new ArrayList<>();
        for (int match = 1; match <= schedule.matchesPerRound(); match++) {
            int home = currentOrder.get(match - 1);
            int away = currentOrder.get(currentOrder.size() - match);
            Trial trial = buildTrial(competition, schedule, match, home, away);
            roundTrials.add(trial);
        }
        return roundTrials;
    }

    private Trial buildTrial(Competition competition, RoundSchedule schedule, int match, int home, int away) {
        int matchNumber = schedule.startMatchNumber() + match - 1;
        Trial trial = new Trial();
        trial.setCompetition(competition);
        trial.setTrialName("Journée " + schedule.round() + " - Match " + match);
        trial.setTrialStartDate(schedule.roundStart());
        trial.setTrialEndDate(schedule.roundEnd());
        trial.setTrialDescription("Journée " + schedule.round() + "/" + schedule.nbRounds()
                + " - Match " + match + "/" + schedule.matchesPerRound()
                + " (match global n°" + matchNumber + ")");
        trial.setTrialStatus(Status.PLANNED);
        trial.setRoundNumber(schedule.round());
        trial.setPosition(match);
        trial.setParticipantIds(buildMatchParticipants(home, away));
        return trial;
    }

    private List<Integer> buildMatchParticipants(int home, int away) {
        return new ArrayList<>(List.of(home, away));
    }
}
