package com.example.FoodHub.security;

import com.example.FoodHub.entity.Role;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.repository.RoleRepository;
import com.example.FoodHub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        String email = oAuth2User.getAttribute("email");

        userRepository.findByEmail(email).orElseGet(() -> {
            Role customerRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new RuntimeException("Role 'CUSTOMER' not found"));

            User user = new User();
            user.setEmail(email);
            user.setUsername(email.split("@")[0]);
            user.setRoleName(customerRole); // ✅ Gán đúng Role entity
            user.setStatus("ACTIVE");
            user.setIsAuthUser(true);
            user.setOauthProvider("GOOGLE");
            user.setPassword("oauth2"); // Có thể đặt dummy nếu không dùng password login
            user.setRegistrationDate(Instant.now());

            return userRepository.save(user);
        });

        return oAuth2User;
    }
}
