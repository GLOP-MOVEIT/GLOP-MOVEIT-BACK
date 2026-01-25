package com.moveit.localization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationHistoryResponse {

    private Long id;
    private Integer userId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
}
