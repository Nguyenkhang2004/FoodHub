package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.UserCreationRequest;
import com.example.FoodHub.dto.request.UserUpdateRequest;
import com.example.FoodHub.dto.response.UserResponse;
import com.example.FoodHub.entity.Role;
import com.example.FoodHub.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roleName", ignore = true)
    User toUser(UserCreationRequest request);

    @Mapping(target = "roleName", source = "roleName.name")
    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);

}
