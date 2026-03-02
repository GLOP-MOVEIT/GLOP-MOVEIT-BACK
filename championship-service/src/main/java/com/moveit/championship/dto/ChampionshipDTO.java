package com.moveit.championship.dto;

import com.moveit.championship.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChampionshipDTO {
    private Integer id;
    private String name;
    private String description;
    private Date startDate;
    private Date endDate;
    private Status status;
    private List<CompetitionSummaryDTO> competitions;
}
