package com.moveit.location.mapper;

import com.moveit.location.dto.Result;
import com.moveit.location.entity.ResultEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResultMapper {

    ResultEntity toEntity(Result result);
    Result toDto(ResultEntity resultEntity);
}