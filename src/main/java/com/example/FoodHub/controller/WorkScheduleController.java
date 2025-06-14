package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.ShiftRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.ShiftResponse;
import com.example.FoodHub.dto.request.EmployeeWorkRequest;
import com.example.FoodHub.dto.response.EmployeeWorkResponse;
import com.example.FoodHub.service.WorkScheduleService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class WorkScheduleController {
    WorkScheduleService workScheduleService;

    @GetMapping
    public ResponseEntity<List<ShiftResponse>> getShiftsForWeek(@RequestParam String weekStart) {
        LocalDate startDate = LocalDate.parse(weekStart);
        List<ShiftResponse> shifts = workScheduleService.getShiftsForWeek(startDate);
        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeWorkResponse>> getEmployeesByRole(@RequestParam String role) {
        List<EmployeeWorkResponse> employees = workScheduleService.getEmployeesByRole(role);
        return ResponseEntity.ok(employees);
    }

    @PostMapping
    public ResponseEntity<ShiftResponse> addShift(@Valid @RequestBody ShiftRequest shiftRequest) {
        ShiftResponse response = workScheduleService.addShift(shiftRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable Integer id) {
        workScheduleService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-work-schedule/today")
    public ResponseEntity<ApiResponse<ShiftResponse>> getMyWorkScheduleToday() {
        ShiftResponse shifts = workScheduleService.getMyWorkScheduleToday();
        ApiResponse<ShiftResponse> response = ApiResponse.<ShiftResponse>builder()
                .code(1000)
                .message("Today's work schedule retrieved successfully")
                .result(shifts)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-work-schedule")
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> getMyWorkSchedule(
            @RequestParam(required = false) String date) {
        List<ShiftResponse> shifts = workScheduleService.getMyWorkSchedule();
        ApiResponse<List<ShiftResponse>> response = ApiResponse.<List<ShiftResponse>>builder()
                .code(1000)
                .message("Work schedule retrieved successfully")
                .result(shifts)
                .build();
        return ResponseEntity.ok(response);
    }

}