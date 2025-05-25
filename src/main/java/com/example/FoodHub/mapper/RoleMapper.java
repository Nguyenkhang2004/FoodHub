package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.RoleRequest;
import com.example.FoodHub.dto.response.RoleResponse;
import com.example.FoodHub.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);
    RoleResponse toRoleResponse(Role role);
}
