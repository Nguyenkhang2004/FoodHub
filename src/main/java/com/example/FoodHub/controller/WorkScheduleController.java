package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.EmployeeDTO;
import com.example.FoodHub.dto.request.ShiftDTO;
import com.example.FoodHub.service.WorkScheduleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkScheduleController {
    WorkScheduleService workScheduleService;

    @GetMapping
    public List<ShiftDTO> getShiftsForWeek(@RequestParam String weekStart) {
        LocalDate startDate = LocalDate.parse(weekStart);
        return workScheduleService.getShiftsForWeek(startDate);
    }

    @GetMapping("/employees")
    public List<EmployeeDTO> getEmployeesByRole(@RequestParam String role) {
        return workScheduleService.getEmployeesByRole(role);
    }

    @PostMapping
    public ShiftDTO addShift(@RequestBody ShiftDTO shiftDTO) {
        return workScheduleService.addShift(shiftDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable Integer id) {
        workScheduleService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }
}