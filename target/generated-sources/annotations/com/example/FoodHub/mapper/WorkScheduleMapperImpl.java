package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.ShiftRequest;
import com.example.FoodHub.dto.response.ShiftResponse;
import com.example.FoodHub.entity.Role;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.entity.WorkSchedule;
import java.time.format.DateTimeFormatter;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-14T21:10:22+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class WorkScheduleMapperImpl implements WorkScheduleMapper {

    private final DateTimeFormatter dateTimeFormatter_dd_MM_yyyy_0650712384 = DateTimeFormatter.ofPattern( "dd/MM/yyyy" );

    @Override
    public WorkSchedule toWorkSchedule(ShiftRequest request) {
        if ( request == null ) {
            return null;
        }

        WorkSchedule workSchedule = new WorkSchedule();

        workSchedule.setId( request.getId() );
        workSchedule.setArea( request.getArea() );

        return workSchedule;
    }

    @Override
    public ShiftResponse toShiftResponse(WorkSchedule workSchedule) {
        if ( workSchedule == null ) {
            return null;
        }

        ShiftResponse shiftResponse = new ShiftResponse();

        shiftResponse.setShift( workSchedule.getShiftType() );
        shiftResponse.setRole( workScheduleUserRoleNameName( workSchedule ) );
        shiftResponse.setName( workScheduleUserUsername( workSchedule ) );
        if ( workSchedule.getWorkDate() != null ) {
            shiftResponse.setDate( dateTimeFormatter_dd_MM_yyyy_0650712384.format( workSchedule.getWorkDate() ) );
        }
        shiftResponse.setId( workSchedule.getId() );
        shiftResponse.setArea( workSchedule.getArea() );

        return shiftResponse;
    }

    @Override
    public void udpateWorkSchedule(ShiftRequest request, WorkSchedule workSchedule) {
        if ( request == null ) {
            return;
        }

        workSchedule.setId( request.getId() );
        workSchedule.setArea( request.getArea() );
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

    private String workScheduleUserUsername(WorkSchedule workSchedule) {
        User user = workSchedule.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getUsername();
    }
}
