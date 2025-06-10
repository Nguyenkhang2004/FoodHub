package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.UserCreationRequest;
import com.example.FoodHub.dto.request.UserUpdateRequest;
import com.example.FoodHub.dto.response.UserResponse;
import com.example.FoodHub.entity.Role;
import com.example.FoodHub.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-10T09:02:49+0700",
    comments = "version: 1.6.0, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
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

        userResponse.roleName( userRoleNameName( user ) );
        userResponse.id( user.getId() );
        userResponse.username( user.getUsername() );
        userResponse.email( user.getEmail() );
        userResponse.password( user.getPassword() );
        userResponse.status( user.getStatus() );
        userResponse.address( user.getAddress() );
        userResponse.phone( user.getPhone() );
        userResponse.isAuthUser( user.getIsAuthUser() );
        userResponse.oauthProvider( user.getOauthProvider() );
        userResponse.registrationDate( user.getRegistrationDate() );

        return userResponse.build();
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

    private String userRoleNameName(User user) {
        Role roleName = user.getRoleName();
        if ( roleName == null ) {
            return null;
        }
        return roleName.getName();
    }
}
