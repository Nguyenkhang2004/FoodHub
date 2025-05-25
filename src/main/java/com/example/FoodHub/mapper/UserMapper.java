package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.UserCreationRequest;
import com.example.FoodHub.dto.request.UserUpdateRequest;
import com.example.FoodHub.dto.response.UserResponse;
import com.example.FoodHub.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.lang.annotation.Target;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roleName", ignore = true)
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
