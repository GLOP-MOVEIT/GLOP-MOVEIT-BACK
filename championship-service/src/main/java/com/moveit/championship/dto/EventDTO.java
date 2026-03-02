package com.moveit.championship.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDTO {
    private UUID eventId;
    private String eventName;
    private LocalDateTime eventDate;
    private String eventDescription;
}
