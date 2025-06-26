package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.ShiftRequest;
import com.example.FoodHub.dto.response.ShiftResponse;
import com.example.FoodHub.entity.WorkSchedule;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
@Mapper(componentModel = "spring")
public interface WorkScheduleMapper {

    @Mapping(source = "user.username", target = "name")
    @Mapping(source = "user.roleName.name", target = "role")
    @Mapping(source = "workDate", target = "date", qualifiedByName = "localDateToInstant")
    @Mapping(source = "shiftType", target = "shift")
    @Mapping(source = "area", target = "area")
    @Mapping(source = "startTime", target = "startTime", qualifiedByName = "localTimeToInstant")
    @Mapping(source = "endTime", target = "endTime", qualifiedByName = "localTimeToInstant")
    ShiftResponse toShiftResponse(WorkSchedule workSchedule, @Context LocalDate workDate);

    @Mapping(source = "date", target = "workDate", qualifiedByName = "stringToLocalDate")
    @Mapping(source = "shift", target = "shiftType")
    @Mapping(target = "user", ignore = true) // Bỏ qua trường user, sẽ gán thủ công
    @Mapping(target = "area", source = "area", conditionExpression = "java(shiftRequest.getRole().equalsIgnoreCase(\"waiter\") && shiftRequest.getArea() != null)")
    @Mapping(target = "startTime", ignore = true) // Bỏ qua, gán thủ công trong service
    @Mapping(target = "endTime", ignore = true) // Bỏ qua, gán thủ công trong service
    WorkSchedule toWorkSchedule(ShiftRequest shiftRequest);

    @Named("localDateToInstant")
    default Instant localDateToInstant(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    @Named("localTimeToInstant")
    default Instant localTimeToInstant(LocalTime localTime, @Context LocalDate workDate) {
        return localTime.atDate(workDate).atZone(ZoneId.of("UTC")).toInstant();
    }

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String date) {
        return LocalDate.parse(date);
    }
}