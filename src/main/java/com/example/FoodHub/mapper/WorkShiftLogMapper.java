package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.response.WorkShiftLogResponse;
import com.example.FoodHub.entity.WorkShiftLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkShiftLogMapper {
    WorkShiftLogResponse toWorkShiftLogResponse(WorkShiftLog workShiftLog);
}
