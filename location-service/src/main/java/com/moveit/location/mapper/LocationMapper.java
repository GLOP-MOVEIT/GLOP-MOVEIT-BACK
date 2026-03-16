package com.moveit.location.mapper;

import com.moveit.location.dto.LocationDTO;
import com.moveit.location.entity.Location;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDTO toDto(Location location);

    Location toEntity(LocationDTO dto);
}