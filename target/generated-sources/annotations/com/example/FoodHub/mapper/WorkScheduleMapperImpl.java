package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.ShiftRequest;
import com.example.FoodHub.dto.response.ShiftResponse;
import com.example.FoodHub.entity.Role;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.entity.WorkSchedule;
import java.time.LocalDate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-27T11:06:15+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class WorkScheduleMapperImpl implements WorkScheduleMapper {

    @Override
    public ShiftResponse toShiftResponse(WorkSchedule workSchedule, LocalDate workDate) {
        if ( workSchedule == null ) {
            return null;
        }

        ShiftResponse shiftResponse = new ShiftResponse();

        shiftResponse.setName( workScheduleUserUsername( workSchedule ) );
        shiftResponse.setRole( workScheduleUserRoleNameName( workSchedule ) );
        shiftResponse.setDate( localDateToInstant( workSchedule.getWorkDate() ) );
        shiftResponse.setShift( workSchedule.getShiftType() );
        shiftResponse.setArea( workSchedule.getArea() );
        shiftResponse.setStartTime( localTimeToInstant( workSchedule.getStartTime(), workDate ) );
        shiftResponse.setEndTime( localTimeToInstant( workSchedule.getEndTime(), workDate ) );
        shiftResponse.setId( workSchedule.getId() );

        return shiftResponse;
    }

    @Override
    public WorkSchedule toWorkSchedule(ShiftRequest shiftRequest) {
        if ( shiftRequest == null ) {
            return null;
        }

        WorkSchedule workSchedule = new WorkSchedule();

        workSchedule.setWorkDate( stringToLocalDate( shiftRequest.getDate() ) );
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
