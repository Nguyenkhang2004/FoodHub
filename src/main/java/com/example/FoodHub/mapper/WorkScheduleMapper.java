package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.ShiftRequest;
import com.example.FoodHub.dto.response.ShiftResponse;
import com.example.FoodHub.entity.WorkSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WorkScheduleMapper {
    WorkSchedule toWorkSchedule(ShiftRequest request);
    @Mapping(source = "shiftType", target = "shift")
    @Mapping(source = "user.roleName.name", target = "role")
    @Mapping(source = "user.username", target = "name")
    @Mapping(source = "workDate", target = "date", dateFormat = "dd/MM/yyyy")
    ShiftResponse toShiftResponse(WorkSchedule workSchedule);
    void udpateWorkSchedule(ShiftRequest request, @MappingTarget WorkSchedule workSchedule);
}
