package com.moveit.championship.mother;

import com.moveit.championship.entity.Championship;
import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.Event;
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
        private Date competitionStartDate = new Date(2026, Calendar.JANUARY, 1);
        private Date competitionEndDate = new Date(2026, Calendar.MARCH, 1);
        private String competitionDescription = "Description de la compétition";
        private Status competitionStatus = Status.PLANNED;
        private List<Event> events = List.of();
        private Integer nbManches = 3;

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
                    nbManches
            );
        }
    }
}
