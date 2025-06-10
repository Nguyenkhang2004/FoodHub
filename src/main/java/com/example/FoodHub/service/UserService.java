package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.EmployeeUpdateRequest;
import com.example.FoodHub.dto.request.UserCreationRequest;
import com.example.FoodHub.dto.response.UserResponse;
import com.example.FoodHub.entity.Role;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.entity.WorkSchedule;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.UserMapper;
import com.example.FoodHub.repository.RoleRepository;
import com.example.FoodHub.repository.UserRepository;
import com.example.FoodHub.repository.WorkScheduleRepository;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    WorkScheduleRepository workScheduleRepository;
    EmailService emailService;
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTS);
        }
        log.info("In method createUser with password: {}", request.getPassword());
        User user = userMapper.toUser(request);
        String plainPassword = request.getPassword();
        user.setPassword(passwordEncoder.encode(plainPassword));
        user.setRoleName(roleRepository.findById(request.getRoleName())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED)));
        user.setStatus("ACTIVE");
        user.setRegistrationDate(Instant.now());
        user.setIsAuthUser(false);
        User savedUser = userRepository.save(user);

        // Gửi email chào mừng
        emailService.sendWelcomeEmailAsync(savedUser.getEmail(), savedUser.getUsername(), plainPassword);

        return userMapper.toUserResponse(savedUser);
    }


    public List<UserResponse> getAllUsers() {
        log.info("In method getAllUsers");
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public UserResponse getUserById(Integer id) {
        return userMapper.toUserResponse(userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

//    public UserResponse updateUser(Integer id, UserUpdateRequest request) {
//        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//        userMapper.updateUser(user, request);
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        userRepository.save(user);
//        return userMapper.toUserResponse(user);
//    }

    public void deleteUser(Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userRepository.delete(user);
    }

    public UserResponse myInfo() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userMapper.toUserResponse(userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }
    public Page<UserResponse> getEmployees(
            String role, String keyword, String sortDirection, int page, int size) {

        // Xác định hướng sắp xếp theo username
        Sort sort = Sort.by("username");
        sort = sortDirection.equalsIgnoreCase("desc") ? sort.descending() : sort.ascending();

        // Tạo Pageable với phân trang và sắp xếp
        Pageable pageable = PageRequest.of(page, size, sort);

        // Lấy danh sách nhân viên từ repository
        Page<User> employeePage = userRepository.findEmployees(role, keyword, pageable);

        // Ánh xạ sang DTO
        return employeePage.map(userMapper::toUserResponse);
    }


    @Transactional
    public void inactiveUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new AppException(ErrorCode.MENU_ITEM_NOT_FOUND);
        }
        LocalDate today = LocalDate.now();
        List<WorkSchedule> futureSchedules = workScheduleRepository.findByUserIdAndDateFromToday(id, today);
        if (!futureSchedules.isEmpty()) {
            throw new AppException(ErrorCode.USER_HAS_SCHEDULED_SHIFTS); // Tạo mã lỗi mới
        }
        int updatedRows = userRepository.updateStatusById(id, "INACTIVE");
        if (updatedRows == 0) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void restoreUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new AppException(ErrorCode.MENU_ITEM_NOT_FOUND);
        }

        int updatedRows =  userRepository.updateStatusById(id, "ACTIVE");
        if (updatedRows == 0) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    public void updateUser(Integer id, EmployeeUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Cập nhật các field trừ password
        userMapper.updateStaff(user, request);

        // Mã hóa mật khẩu mới
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Cập nhật role nếu cần
        Role role = roleRepository.findById(request.getRoleName())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        user.setRoleName(role);

        userRepository.save(user);
    }
    public long countUser() {
        return userRepository.count();
    }
}

