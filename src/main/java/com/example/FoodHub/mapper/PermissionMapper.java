package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.PermissionRequest;
import com.example.FoodHub.dto.response.PermissionResponse;
import com.example.FoodHub.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
