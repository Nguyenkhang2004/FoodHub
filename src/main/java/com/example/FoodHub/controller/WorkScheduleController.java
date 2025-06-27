package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.ShiftRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.ShiftResponse;
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
@CrossOrigin(origins = "http://127.0.0.1:5500")
@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class WorkScheduleController {
    WorkScheduleService workScheduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> getShiftsForWeek(@RequestParam String weekStart) {
        LocalDate startDate = LocalDate.parse(weekStart);
        List<ShiftResponse> shifts = workScheduleService.getShiftsForWeek(startDate);
        ApiResponse<List<ShiftResponse>> response = ApiResponse.<List<ShiftResponse>>builder()
                .code(1000)
                .message("Lấy danh sách ca làm việc thành công")
                .result(shifts)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employees")
    public ResponseEntity<ApiResponse<List<EmployeeWorkResponse>>> getEmployeesByRole(@RequestParam String role) {
        List<EmployeeWorkResponse> employees = workScheduleService.getEmployeesByRole(role);
        ApiResponse<List<EmployeeWorkResponse>> response = ApiResponse.<List<EmployeeWorkResponse>>builder()
                .code(1000)
                .message("Lấy danh sách nhân viên thành công")
                .result(employees)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShiftResponse>> addShift(@Valid @RequestBody ShiftRequest shiftRequest) {
        ShiftResponse response = workScheduleService.addShift(shiftRequest);
        ApiResponse<ShiftResponse> apiResponse = ApiResponse.<ShiftResponse>builder()
                .code(1000)
                .message("Thêm ca làm việc thành công")
                .result(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShift(@PathVariable Integer id) {
        workScheduleService.deleteShift(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(1000)
                .message("Ca làm việc đã được xóa thành công")
                .result(null)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/my-work-schedule/today")
    public ResponseEntity<ApiResponse<ShiftResponse>> getMyWorkScheduleToday() {
        ShiftResponse shifts = workScheduleService.getMyCurrentWorkScheduleToday();
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