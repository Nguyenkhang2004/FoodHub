package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.EmployeeUpdateRequest;
import com.example.FoodHub.dto.request.OtpRequest;
import com.example.FoodHub.dto.request.UserCreationRequest;
import com.example.FoodHub.dto.response.UserResponse;
import com.example.FoodHub.entity.OtpVerification;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.UserMapper;
import com.example.FoodHub.repository.OtpVerificationRepository;
import com.example.FoodHub.repository.RoleRepository;
import com.example.FoodHub.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    OtpVerificationRepository otpVerificationRepository;
    EmailService emailService;
    RoleRepository roleRepository;

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
        user.setStatus("ACTIVE");
        user.setRegistrationDate(LocalDateTime.now().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());
        user.setIsAuthUser(false);
        User savedUser = userRepository.save(user);
        emailService.sendWelcomeEmailAsync(savedUser.getEmail(), savedUser.getUsername(), null);
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

    public UserResponse getUserById(Integer id) {
        return userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userRepository.delete(user);
    }

    public UserResponse myInfo() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userMapper.toUserResponse(userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    public Page<UserResponse> getEmployees(String role, String keyword, String sortDirection, int page, int size) {
        Sort sort = Sort.by("username");
        sort = sortDirection.equalsIgnoreCase("desc") ? sort.descending() : sort.ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> employeePage = userRepository.findEmployees(role, keyword, pageable);
        return employeePage.map(userMapper::toUserResponse);
    }

    @Transactional
    public void inactiveUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        int updatedRows = userRepository.updateStatusById(id, "INACTIVE");
        if (updatedRows == 0) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void restoreUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        int updatedRows = userRepository.updateStatusById(id, "ACTIVE");
        if (updatedRows == 0) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void updateUser(Integer id, EmployeeUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateStaff(user, request);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
    }

    public long countUser() {
        return userRepository.count();
    }
}