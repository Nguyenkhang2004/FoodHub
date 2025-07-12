package com.example.FoodHub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkShiftLogResponse {
    Integer id;
    Instant workScheduleDate;
    Instant checkInTime;
    Instant checkOutTime;
    String status; // ON_TIME, LATE, LEFT_EARLY, ABSENT, UNSCHEDULED
}
