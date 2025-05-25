package com.example.FoodHub.controller;


import com.example.FoodHub.dto.request.AuthenticationRequest;
import com.example.FoodHub.dto.request.IntrospectRequest;
import com.example.FoodHub.dto.request.LogoutRequest;
import com.example.FoodHub.dto.request.RefreshRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.AuthenticationResponse;
import com.example.FoodHub.dto.response.IntrospectResponse;
import com.example.FoodHub.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request){
        AuthenticationResponse result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
       IntrospectResponse result = authenticationService.introspect(request);
       return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) throws ParseException, JOSEException {
        AuthenticationResponse result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }
}
