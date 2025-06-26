package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.*;
import com.example.FoodHub.dto.response.UserResponse;
import com.example.FoodHub.entity.OtpVerification;
import com.example.FoodHub.entity.Role;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.entity.WorkSchedule;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.UserMapper;
import com.example.FoodHub.repository.OtpVerificationRepository;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Random;

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
    OtpVerificationRepository otpVerificationRepository;


    @Transactional
    public UserResponse createUser(UserCreationRequest request) {
        if (!roleRepository.existsByName(request.getRoleName())) {
            throw new AppException(ErrorCode.INVALID_ROLE);
        }
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

    @Transactional
    public void sendOtp(UserCreationRequest request) {
        if (!roleRepository.existsByName(request.getRoleName())) {
            throw new AppException(ErrorCode.INVALID_ROLE);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTS);
        }
        String otp = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
        OtpVerification otpVerification = OtpVerification.builder()
                .email(request.getEmail())
                .otp(otp)
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .roleName(request.getRoleName())
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();
        otpVerificationRepository.save(otpVerification);
        emailService.sendOtpEmailAsync(request.getEmail(), otp);
    }

    @Transactional
    public UserResponse verifyOtpAndCreateUser(OtpRequest request) {
        OtpVerification otpVerification = otpVerificationRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_FOUND));
        if (!otpVerification.getOtp().equals(request.getOtp())) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }
        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }
        if (userRepository.existsByUsername(otpVerification.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByEmail(otpVerification.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTS);
        }
        User user = User.builder()
                .username(otpVerification.getUsername())
                .password(otpVerification.getPassword())
                .email(otpVerification.getEmail())
                .phone(otpVerification.getPhone())
                .address(otpVerification.getAddress())
                .roleName(userMapper.map(otpVerification.getRoleName()))
                .status("ACTIVE")
                .registrationDate(LocalDateTime.now().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant())                .isAuthUser(false)
                .build();
        userRepository.save(user);
        emailService.sendWelcomeEmailAsync(user.getEmail(), user.getUsername(), null);
        otpVerificationRepository.delete(otpVerification);
        return userMapper.toUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        log.info("In method getAllUsers");
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }
    public Page<UserResponse> getCustomers(String keyword, String status ,String sortDirection, int page, int size) {
        log.info("In method getCustomers");
        // Xác định hướng sắp xếp theo username
        Sort sort = Sort.by("username");
        sort = sortDirection.equalsIgnoreCase("desc") ? sort.descending() : sort.ascending();

        // Tạo Pageable với phân trang và sắp xếp
        Pageable pageable = PageRequest.of(page, size, sort);

        // Lấy danh sách khách hàng từ repository
        Page<User> customerPage = userRepository.findCustomers(keyword,status, pageable);

        // Ánh xạ sang DTO
        return customerPage.map(userMapper::toUserResponse);
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
            String role, String keyword, String status, String sortDirection, int page, int size) {

        // Xác định hướng sắp xếp theo username
        Sort sort = Sort.by("username");
        sort = sortDirection.equalsIgnoreCase("desc") ? sort.descending() : sort.ascending();

        // Tạo Pageable với phân trang và sắp xếp
        Pageable pageable = PageRequest.of(page, size, sort);

        // Lấy danh sách nhân viên từ repository
        Page<User> employeePage = userRepository.findEmployees(role, keyword, status, pageable);

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

    @Transactional
    public void updateUser(Integer id, EmployeeUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Cập nhật các field trừ password
        userMapper.updateStaff(user, request);

        // Mã hóa mật khẩu mới
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
    }

    public void updateCustomer(Integer userId, UserUpdateRequest request) {
        // B1: Tìm user theo ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user ID: " + userId));


        user.setEmail(request.getEmail());
        user.setPhone(request.getPhoneNumber());
        user.setAddress(request.getAddress());

        // B3: Lưu lại
        userRepository.save(user);
    }
    public long getTotalCustomers() {
        return userRepository.countCustomers();
    }


    public long getTotalEmployees() {
        return userRepository.countEmployees();
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    @Transactional
    public void sendOtpForPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String otp = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        // Ghi đè OTP cũ nếu có
        OtpVerification otpVerification = otpVerificationRepository.findByEmail(email)
                .orElse(new OtpVerification());

        otpVerification.setEmail(email);
        otpVerification.setOtp(otp);
        otpVerification.setCreatedAt(LocalDateTime.now());
        otpVerification.setExpiresAt(expiresAt);

        otpVerification.setAddress(user.getAddress() != null ? user.getAddress() : "N/A");
        otpVerification.setPhone(user.getPhone() != null ? user.getPhone() : "0000000000");
        otpVerification.setUsername(user.getUsername());
        otpVerification.setRoleName("CUSTOMER");
        otpVerification.setPassword(user.getPassword());

        otpVerificationRepository.save(otpVerification);

        emailService.sendOtpEmailAsync(email, otp);
    }

    @Transactional
    public void resetPasswordWithOtp(ResetPasswordWithOtpRequest request) {
        String email = request.getEmail();
        String submittedOtp = request.getOtp();
        String newPassword = request.getNewPassword();

        OtpVerification otpVerification = otpVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_FOUND));

        if (!otpVerification.getOtp().equals(submittedOtp)) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpVerificationRepository.delete(otpVerification);
    }



}