package com.example.FoodHub.utils;

import java.time.*;

public class TimeUtils {
    public static Instant getNowInVietNam(){
        return LocalDateTime.now().toInstant(ZoneOffset.UTC);
    }

}
