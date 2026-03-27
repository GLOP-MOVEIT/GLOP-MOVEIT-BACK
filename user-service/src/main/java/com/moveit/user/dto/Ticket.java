package com.moveit.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    private Integer id;
    @NotBlank(message = "Ticket number is required")
    private String ticketNumber;
    @NotBlank(message = "Seat information is required")
    private String seatInformation;
    private Instant eventDate;
    private String validationToken;
}
