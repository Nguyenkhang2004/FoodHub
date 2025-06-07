package com.example.FoodHub.service;
import com.example.FoodHub.dto.request.EmployeeDTO;
import com.example.FoodHub.dto.request.ShiftDTO;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.entity.WorkSchedule;
import com.example.FoodHub.repository.UserRepository;
import com.example.FoodHub.repository.WorkScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkScheduleService {
    @Autowired
    private WorkScheduleRepository workScheduleRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ShiftDTO> getShiftsForWeek(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<WorkSchedule> schedules = workScheduleRepository.findByWeek(weekStart, weekEnd);
        return schedules.stream().map(this::convertToShiftDTO).collect(Collectors.toList());
    }

    public List<EmployeeDTO> getEmployeesByRole(String role) {
        List<User> users = userRepository.findByRoleName_Name(role);
        return users.stream().map(user -> {
            EmployeeDTO dto = new EmployeeDTO();
            dto.setName(user.getUsername());
            dto.setRole(user.getRoleName().getName());
            return dto;
        }).collect(Collectors.toList());
    }

    public ShiftDTO addShift(ShiftDTO shiftDTO) {
        User user = userRepository.findByUsername(shiftDTO.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDate shiftDate = LocalDate.parse(shiftDTO.getDate());
        LocalDate today = LocalDate.now();

        if (shiftDate.isBefore(today)) {
            throw new IllegalArgumentException("Không thể xếp ca làm việc cho ngày trước hôm nay");
        }

        List<WorkSchedule> existingSchedules = workScheduleRepository.findByDate(shiftDate);
        for (WorkSchedule schedule : existingSchedules) {
            if (schedule.getUser().getUsername().equals(shiftDTO.getName()) &&
                    schedule.getShiftType().equalsIgnoreCase(shiftDTO.getShift().toUpperCase())) {
                throw new IllegalArgumentException("Nhân viên đã được xếp ca " + shiftDTO.getShift() + " vào ngày này");
            }
        }
        WorkSchedule schedule = new WorkSchedule();
        schedule.setUser(user);
        schedule.setWorkDate(LocalDate.parse(shiftDTO.getDate()));
        schedule.setShiftType(shiftDTO.getShift().toUpperCase());

        // Set start and end times based on shift type
        switch (shiftDTO.getShift().toLowerCase()) {
            case "morning":
                schedule.setStartTime(LocalTime.of(8, 30));
                schedule.setEndTime(LocalTime.of(12, 30));
                break;
            case "afternoon":
                schedule.setStartTime(LocalTime.of(12, 30));
                schedule.setEndTime(LocalTime.of(17, 30));
                break;
            case "night":
                schedule.setStartTime(LocalTime.of(17, 30));
                schedule.setEndTime(LocalTime.of(22, 30));
                break;
            default:
                throw new IllegalArgumentException("Invalid shift type");
        }

        WorkSchedule savedSchedule = workScheduleRepository.save(schedule);
        return convertToShiftDTO(savedSchedule);
    }

    public void deleteShift(Integer id) {
        workScheduleRepository.deleteById(id);
    }

    private ShiftDTO convertToShiftDTO(WorkSchedule schedule) {
        ShiftDTO dto = new ShiftDTO();
        dto.setId(schedule.getId());
        dto.setName(schedule.getUser().getUsername());
        dto.setRole(schedule.getUser().getRoleName().getName().toLowerCase());
        dto.setDate(schedule.getWorkDate().toString());
        dto.setShift(schedule.getShiftType().toLowerCase());
        return dto;
    }

}