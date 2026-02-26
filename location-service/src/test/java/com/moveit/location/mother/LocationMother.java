package com.moveit.location.mother;

import com.moveit.location.entity.Location;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;

public class LocationMother {
    public static Builder location() {
        return new Builder();
    }

    @With
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Builder {
        private Integer locationId = 1;
        private String name = "Stade de France";
        private Double latitude = 48.9244;
        private Double longitude = 2.3601;
        private String mainEntrance = "Avenue Jules Rimet, 93200 Saint-Denis";
        private String refereeEntrance = "Entrée E - Vestiaires arbitres";
        private String athleteEntrance = "Entrée B - Vestiaires équipes";
        private String description = "Grand stade national français";

        public Location build() {
            return new Location(
                    locationId,
                    name,
                    latitude,
                    longitude,
                    mainEntrance,
                    refereeEntrance,
                    athleteEntrance,
                    description
            );
        }
    }
}
