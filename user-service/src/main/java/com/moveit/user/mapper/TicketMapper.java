package com.moveit.user.mapper;

import com.moveit.user.dto.Ticket;
import com.moveit.user.entity.TicketEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    Ticket toDto(TicketEntity ticketEntity);
    TicketEntity toEntity(Ticket ticket);
}