package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.response.WorkShiftLogResponse;
import com.example.FoodHub.entity.WorkShiftLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface WorkShiftLogMapper {
    @Mapping(source = "workSchedule.workDate", target = "workScheduleDate", qualifiedByName = "localDateToInstant")
    WorkShiftLogResponse toWorkShiftLogResponse(WorkShiftLog workShiftLog);
    @Named("localDateToInstant")
    default Instant localDateToInstant(LocalDate localDate) {
        if (localDate == null) return null;
        return localDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

}
