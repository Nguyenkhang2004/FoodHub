package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.EmployeeUpdateRequest;
import com.example.FoodHub.dto.request.UserCreationRequest;
import com.example.FoodHub.dto.request.UserUpdateRequest;
import com.example.FoodHub.dto.response.UserResponse;
import com.example.FoodHub.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-07T09:54:37+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toUser(UserCreationRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.username( request.getUsername() );
        user.email( request.getEmail() );
        user.password( request.getPassword() );
        user.address( request.getAddress() );
        user.phone( request.getPhone() );

        return user.build();
    }

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.id( user.getId() );
        userResponse.username( user.getUsername() );
        userResponse.email( user.getEmail() );
        userResponse.phone( user.getPhone() );
        userResponse.status( user.getStatus() );
        userResponse.address( user.getAddress() );
        userResponse.registrationDate( user.getRegistrationDate() );

        userResponse.roleName( user.getRoleName() != null ? toRoleResponse(user.getRoleName()) : null );

        return userResponse.build();
    }

    @Override
    public void updateUser(User user, UserCreationRequest request) {
        if ( request == null ) {
            return;
        }

        user.setUsername( request.getUsername() );
        user.setEmail( request.getEmail() );
        user.setPassword( request.getPassword() );
        user.setRoleName( map( request.getRoleName() ) );
        user.setAddress( request.getAddress() );
        user.setPhone( request.getPhone() );
    }

    @Override
    public void updateStaff(User user, EmployeeUpdateRequest request) {
        if ( request == null ) {
            return;
        }

        user.setUsername( request.getUsername() );
        user.setEmail( request.getEmail() );
        user.setRoleName( map( request.getRoleName() ) );
        user.setStatus( request.getStatus() );
        user.setAddress( request.getAddress() );
        user.setPhone( request.getPhone() );
        user.setOauthProvider( request.getOauthProvider() );
    }

    @Override
    public void updateUser(User user, UserUpdateRequest request) {
        if ( request == null ) {
            return;
        }

        user.setEmail( request.getEmail() );
        user.setPassword( request.getPassword() );
        user.setAddress( request.getAddress() );
    }
}
