package com.example.FoodHub.security;

import com.example.FoodHub.dto.response.AuthenticationResponse;
import com.example.FoodHub.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        AuthenticationResponse tokenResponse = authenticationService.authenticateByEmail(email);

        // Redirect về HTML login giao diện frontend, gắn token
        String redirect = "http://127.0.0.1:5500/home-page/html/login.html?token=" + tokenResponse.getToken();
        response.sendRedirect(redirect);
    }
}
