package com.example.FoodHub.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class TimeUtils {
    public static Instant getNowInVietNam(){
        return LocalDateTime.now().toInstant(ZoneOffset.UTC);
    }
}
