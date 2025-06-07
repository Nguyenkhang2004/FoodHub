package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.EmployeeUpdateRequest;
import com.example.FoodHub.dto.request.UserCreationRequest;
import com.example.FoodHub.dto.request.UserUpdateRequest;
import com.example.FoodHub.dto.response.RoleResponse;
import com.example.FoodHub.dto.response.UserResponse;
import com.example.FoodHub.entity.Role;
import com.example.FoodHub.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {


    @Mapping(target = "roleName", ignore = true)
    User toUser(UserCreationRequest request);

    @Mapping(target = "roleName", expression = "java(toRoleResponse(user.getRoleName()))")
    UserResponse toUserResponse(User user);

    @Mapping(target = "password", ignore = true) // <- thêm dòng này
    void updateStaff(@MappingTarget User user, EmployeeUpdateRequest request);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    default Role map(String roleName) {
        if (roleName == null) return null;
        Role role = new Role();
        role.setName(roleName);
        return role;
    }
    default RoleResponse toRoleResponse(Role role) {
        if (role == null) return null;
        return RoleResponse.builder()
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}  