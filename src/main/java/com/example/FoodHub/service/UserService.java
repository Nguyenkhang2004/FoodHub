package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.UserCreationRequest;
import com.example.FoodHub.dto.response.UserResponse;
import com.example.FoodHub.entity.Role;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.repository.RoleRepository;
import com.example.FoodHub.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {
        return userRepository.findByRoleName_Name("CUSTOMER").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
        if (!"CUSTOMER".equals(user.getRoleName().getName())) {
            throw new RuntimeException("Người dùng không có vai trò CUSTOMER");
        }
        return convertToDTO(user);
    }

    @Transactional
    public UserResponse createUser(UserCreationRequest userRequest) {
        if (!"CUSTOMER".equals(userRequest.getRoleName().getName())) {
            throw new RuntimeException("Chỉ được phép tạo người dùng với vai trò CUSTOMER");
        }

        validateUserDTO(userRequest, false);

        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
        user.setAddress(userRequest.getAddress());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRoleName(findRoleByName(userRequest.getRoleName().getName()));
        user.setStatus("ACTIVE");
        user.setIsAuthUser(false);
        user.setOauthProvider(null);

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Transactional
    public UserResponse updateUser(Integer id, UserResponse userResponse) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
        if (!"CUSTOMER".equals(existingUser.getRoleName().getName())) {
            throw new RuntimeException("Người dùng không có vai trò CUSTOMER");
        }
        if (!"CUSTOMER".equals(userResponse.getRoleName())) {
            throw new RuntimeException("Chỉ được phép cập nhật người dùng với vai trò CUSTOMER");
        }

        existingUser.setUsername(userResponse.getUsername());
        existingUser.setEmail(userResponse.getEmail());
        if (userResponse.getPassword() != null && !userResponse.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userResponse.getPassword()));
        }
        existingUser.setStatus(userResponse.getStatus());
        existingUser.setAddress(userResponse.getAddress());
        existingUser.setPhone(userResponse.getPhone());
        existingUser.setIsAuthUser(userResponse.getIsAuthUser());
        existingUser.setOauthProvider(userResponse.getOauthProvider());

        userRepository.save(existingUser);
        return convertToDTO(existingUser);
    }

    @Transactional
    public void deactivateUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
        if (!"CUSTOMER".equals(user.getRoleName().getName())) {
            throw new RuntimeException("Người dùng không có vai trò CUSTOMER");
        }
        user.setStatus("INACTIVE");
        userRepository.save(user);
    }

    private void validateUserDTO(UserCreationRequest userRequest, boolean isUpdate) {
        if (!isUpdate) {
            if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
                throw new RuntimeException("Tên người dùng đã tồn tại");
            }
            if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
                throw new RuntimeException("Email đã tồn tại");
            }
        }
        if (userRequest.getRoleName() == null || !"CUSTOMER".equals(userRequest.getRoleName().getName())) {
            throw new RuntimeException("Vai trò không hợp lệ, chỉ được phép là CUSTOMER");
        }
    }

    private UserResponse convertToDTO(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .roleName(user.getRoleName().getName())
                .status(user.getStatus())
                .address(user.getAddress())
                .phone(user.getPhone())
                .isAuthUser(user.getIsAuthUser())
                .oauthProvider(user.getOauthProvider())
                .build();
    }

    private Role findRoleByName(String roleName) {
        return roleRepository.findById(roleName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò: " + roleName));
    }
}