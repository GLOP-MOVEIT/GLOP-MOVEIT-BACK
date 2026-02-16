package com.moveit.championship.mother;

import com.moveit.championship.entity.Championship;
import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.CompetitionType;
import com.moveit.championship.entity.Event;
import com.moveit.championship.entity.ParticipantType;
import com.moveit.championship.entity.Trial;
import com.moveit.championship.entity.Status;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CompetitionMother {
    public static Builder competition() {
        return new Builder();
    }

    @With
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Builder {
        private Integer competitionId = 1;
        private Championship championship = ChampionshipMother.championship().build();
        private String competitionSport = "Football";
        private String competitionName = "Ligue 1";
        private Date competitionStartDate = createDate(2026, Calendar.JANUARY, 1);
        private Date competitionEndDate = createDate(2026, Calendar.MARCH, 1);
        private String competitionDescription = "Description de la compétition";
        private Status competitionStatus = Status.PLANNED;
        private List<Event> events = List.of();
        private List<Trial> trials = List.of();
        private Integer nbManches = 3;
        private CompetitionType competitionType = CompetitionType.SINGLE_ELIMINATION;
        private Integer maxPerHeat = null;
        private ParticipantType participantType = ParticipantType.INDIVIDUAL;

        private static Date createDate(int year, int month, int day) {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day, 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }

        public Competition build() {
                return new Competition(
                    competitionId,
                    championship,
                    competitionSport,
                    competitionName,
                    competitionStartDate,
                    competitionEndDate,
                    competitionDescription,
                    competitionStatus,
                    events,
                    trials,
                    nbManches,
                    competitionType,
                    maxPerHeat,
                    participantType
                );
        }
    }
}
