package com.example.FoodHub.service;


import com.example.FoodHub.dto.response.UserDTO;
import com.example.FoodHub.entity.Role;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.repository.RoleRepository;
import com.example.FoodHub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findByRoleName_Name("CUSTOMER").stream() // Chỉ lấy người dùng có roleName là CUSTOMER
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
        if (!"CUSTOMER".equals(user.getRoleName().getName())) {
            throw new RuntimeException("Người dùng không có vai trò CUSTOMER");
        }
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        if (!"CUSTOMER".equals(userDTO.getRoleName())) {
            throw new RuntimeException("Chỉ được phép tạo người dùng với vai trò CUSTOMER");
        }
        validateUserDTO(userDTO, false);
        User user = convertToEntity(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Integer id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
        if (!"CUSTOMER".equals(existingUser.getRoleName().getName())) {
            throw new RuntimeException("Người dùng không có vai trò CUSTOMER");
        }
        if (!"CUSTOMER".equals(userDTO.getRoleName())) {
            throw new RuntimeException("Chỉ được phép cập nhật người dùng với vai trò CUSTOMER");
        }
        validateUserDTO(userDTO, true);

        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        existingUser.setRoleName(findRoleByName(userDTO.getRoleName()));
        existingUser.setStatus(userDTO.getStatus());
        existingUser.setAddress(userDTO.getAddress());
        existingUser.setPhone(userDTO.getPhone());
        existingUser.setIsAuthUser(userDTO.getIsAuthUser());
        existingUser.setOauthProvider(userDTO.getOauthProvider());

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

    private void validateUserDTO(UserDTO userDTO, boolean isUpdate) {
        if (!isUpdate) {
            if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
                throw new RuntimeException("Tên người dùng đã tồn tại");
            }
            if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
                throw new RuntimeException("Email đã tồn tại");
            }
        }
        if (!"CUSTOMER".equals(userDTO.getRoleName())) {
            throw new RuntimeException("Vai trò không hợp lệ, chỉ được phép là CUSTOMER");
        }
        if (!List.of("ACTIVE", "INACTIVE").contains(userDTO.getStatus())) {
            throw new RuntimeException("Trạng thái không hợp lệ");
        }
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleName(user.getRoleName().getName())
                .status(user.getStatus())
                .address(user.getAddress())
                .phone(user.getPhone())
                .isAuthUser(user.getIsAuthUser())
                .oauthProvider(user.getOauthProvider())
                .build();
    }

    private User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword()); // Will be encoded later
        user.setRoleName(findRoleByName(userDTO.getRoleName()));
        user.setStatus(userDTO.getStatus());
        user.setAddress(userDTO.getAddress());
        user.setPhone(userDTO.getPhone());
        user.setIsAuthUser(userDTO.getIsAuthUser());
        user.setOauthProvider(userDTO.getOauthProvider());
        return user;
    }

    private Role findRoleByName(String roleName) {
        return roleRepository.findById(roleName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò: " + roleName));
    }


}