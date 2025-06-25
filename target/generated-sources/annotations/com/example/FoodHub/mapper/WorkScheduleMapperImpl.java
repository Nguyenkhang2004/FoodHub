package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.ShiftRequest;
import com.example.FoodHub.dto.response.ShiftResponse;
import com.example.FoodHub.entity.Role;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.entity.WorkSchedule;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-25T15:03:14+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 21.0.4 (Oracle Corporation)"
)
@Component
public class WorkScheduleMapperImpl implements WorkScheduleMapper {

    private final DateTimeFormatter dateTimeFormatter_yyyy_MM_dd_0159776256 = DateTimeFormatter.ofPattern( "yyyy-MM-dd" );
    private final DateTimeFormatter dateTimeFormatter_HH_mm_168697690 = DateTimeFormatter.ofPattern( "HH:mm" );

    @Override
    public ShiftResponse toShiftResponse(WorkSchedule workSchedule) {
        if ( workSchedule == null ) {
            return null;
        }

        ShiftResponse shiftResponse = new ShiftResponse();

        shiftResponse.setName( workScheduleUserUsername( workSchedule ) );
        shiftResponse.setRole( workScheduleUserRoleNameName( workSchedule ) );
        if ( workSchedule.getWorkDate() != null ) {
            shiftResponse.setDate( dateTimeFormatter_yyyy_MM_dd_0159776256.format( workSchedule.getWorkDate() ) );
        }
        shiftResponse.setShift( workSchedule.getShiftType() );
        shiftResponse.setArea( workSchedule.getArea() );
        if ( workSchedule.getStartTime() != null ) {
            shiftResponse.setStartTime( dateTimeFormatter_HH_mm_168697690.format( workSchedule.getStartTime() ) );
        }
        if ( workSchedule.getEndTime() != null ) {
            shiftResponse.setEndTime( dateTimeFormatter_HH_mm_168697690.format( workSchedule.getEndTime() ) );
        }
        shiftResponse.setId( workSchedule.getId() );

        return shiftResponse;
    }

    @Override
    public WorkSchedule toWorkSchedule(ShiftRequest shiftRequest) {
        if ( shiftRequest == null ) {
            return null;
        }

        WorkSchedule workSchedule = new WorkSchedule();

        if ( shiftRequest.getDate() != null ) {
            workSchedule.setWorkDate( LocalDate.parse( shiftRequest.getDate() ) );
        }
        workSchedule.setShiftType( shiftRequest.getShift() );
        if ( shiftRequest.getRole().equalsIgnoreCase("waiter") && shiftRequest.getArea() != null ) {
            workSchedule.setArea( shiftRequest.getArea() );
        }
        workSchedule.setId( shiftRequest.getId() );

        return workSchedule;
    }

    private String workScheduleUserUsername(WorkSchedule workSchedule) {
        User user = workSchedule.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getUsername();
    }

    private String workScheduleUserRoleNameName(WorkSchedule workSchedule) {
        User user = workSchedule.getUser();
        if ( user == null ) {
            return null;
        }
        Role roleName = user.getRoleName();
        if ( roleName == null ) {
            return null;
        }
        return roleName.getName();
    }
}
