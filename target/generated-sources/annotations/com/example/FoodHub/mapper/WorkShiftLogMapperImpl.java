package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.response.WorkShiftLogResponse;
import com.example.FoodHub.entity.WorkShiftLog;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-08T17:55:25+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class WorkShiftLogMapperImpl implements WorkShiftLogMapper {

    @Override
    public WorkShiftLogResponse toWorkShiftLogResponse(WorkShiftLog workShiftLog) {
        if ( workShiftLog == null ) {
            return null;
        }

        WorkShiftLogResponse.WorkShiftLogResponseBuilder workShiftLogResponse = WorkShiftLogResponse.builder();

        workShiftLogResponse.id( workShiftLog.getId() );
        workShiftLogResponse.checkInTime( workShiftLog.getCheckInTime() );
        workShiftLogResponse.checkOutTime( workShiftLog.getCheckOutTime() );
        workShiftLogResponse.status( workShiftLog.getStatus() );

        return workShiftLogResponse.build();
    }
}
