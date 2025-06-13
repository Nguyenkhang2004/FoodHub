package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.PermissionRequest;
import com.example.FoodHub.dto.response.PermissionResponse;
import com.example.FoodHub.entity.Permission;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
<<<<<<< HEAD
<<<<<<< HEAD
    date = "2025-06-11T07:37:57+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 21.0.4 (Oracle Corporation)"
=======
    date = "2025-06-11T15:58:05+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
>>>>>>> order
=======
    date = "2025-06-13T20:59:20+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 22.0.2 (Amazon.com Inc.)"
>>>>>>> 9db9138dae1eb07dbfc1261a040dd6e245a37d85
)
@Component
public class PermissionMapperImpl implements PermissionMapper {

    @Override
    public Permission toPermission(PermissionRequest request) {
        if ( request == null ) {
            return null;
        }

        Permission permission = new Permission();

        permission.setName( request.getName() );
        permission.setDescription( request.getDescription() );

        return permission;
    }

    @Override
    public PermissionResponse toPermissionResponse(Permission permission) {
        if ( permission == null ) {
            return null;
        }

        PermissionResponse.PermissionResponseBuilder permissionResponse = PermissionResponse.builder();

        permissionResponse.name( permission.getName() );
        permissionResponse.description( permission.getDescription() );

        return permissionResponse.build();
    }
}
