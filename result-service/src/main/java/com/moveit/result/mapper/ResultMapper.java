package com.moveit.result.mapper;

import com.moveit.result.dto.Result;
import com.moveit.result.entity.ResultEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResultMapper {

    ResultEntity toEntity(Result result);
    Result toDto(ResultEntity resultEntity);
}