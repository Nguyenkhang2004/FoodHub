package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.ShiftRequest;
import com.example.FoodHub.dto.response.ShiftResponse;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.entity.WorkSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkScheduleMapper {

    @Mapping(source = "user.username", target = "name")
    @Mapping(source = "user.roleName.name", target = "role")
    @Mapping(source = "workDate", target = "date", dateFormat = "yyyy-MM-dd")
    @Mapping(source = "shiftType", target = "shift")
    @Mapping(source = "area", target = "area")
    @Mapping(source = "startTime", target = "startTime", dateFormat = "HH:mm")
    @Mapping(source = "endTime", target = "endTime", dateFormat = "HH:mm")
    ShiftResponse toShiftResponse(WorkSchedule workSchedule);

    @Mapping(source = "date", target = "workDate")
    @Mapping(source = "shift", target = "shiftType")
    @Mapping(target = "user", ignore = true) // Bỏ qua trường user, sẽ gán thủ công
    @Mapping(target = "area", source = "area", conditionExpression = "java(shiftRequest.getRole().equalsIgnoreCase(\"waiter\") && shiftRequest.getArea() != null)")
    WorkSchedule toWorkSchedule(ShiftRequest shiftRequest);

}