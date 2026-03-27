package com.moveit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketVerificationResponse {

    private Integer ticketId;
    private String ticketNumber;
    private String seatInformation;
    private Instant eventDate;
    private boolean valid;
}
