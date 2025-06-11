package com.example.FoodHub.controller;


import com.example.FoodHub.dto.request.*;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.AuthenticationResponse;
import com.example.FoodHub.dto.response.IntrospectResponse;
import com.example.FoodHub.dto.response.UserResponse;
import com.example.FoodHub.service.AuthenticationService;
import com.example.FoodHub.service.UserService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(@RequestBody AuthenticationRequest request){
        AuthenticationResponse result = authenticationService.authenticate(request);
        ApiResponse<AuthenticationResponse> response = ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody UserCreationRequest request) throws ParseException, JOSEException {
        UserResponse result = userService.createUser(request);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(result)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        IntrospectResponse result = authenticationService.introspect(request);
        ApiResponse<IntrospectResponse> response = ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refresh(@RequestBody RefreshRequest request) throws ParseException, JOSEException {
        AuthenticationResponse result = authenticationService.refreshToken(request);
        ApiResponse<AuthenticationResponse> response = ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
        return ResponseEntity.ok().body(response);
    }
}