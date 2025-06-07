package com.example.FoodHub.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private CustomJwtDecoder customJwtDecoder;
    private final String[] PUBLIC_ENDPOINTS = {
            "/users",
            "/auth/login",
            "/auth/introspect",
            "/auth/logout",
            "/auth/refresh",
            "/menu",
            "/public/**",
            "/admin/**",
            "/waiter/**",
            "/orders/**",
            "/login.html",
            "/index.html",
            "/adminDashboard/adminDashboard.html",
            "/adminDashboard/adminDashboardUser.html"
    };
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
//        httpSecurity.authorizeHttpRequests(requests -> requests
//                .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
//                .requestMatchers("/js/**", "/css/**", "/images/**", "/scss/**", "/lib/**").permitAll()
//                .requestMatchers(HttpMethod.GET, PUBLIC_ENDPOINTS).permitAll()
//                .anyRequest().authenticated());
//        httpSecurity.oauth2ResourceServer(httpSecurityOAuth2ResourceServerConfigurer -> httpSecurityOAuth2ResourceServerConfigurer
//                .jwt(jwtConfigurer -> jwtConfigurer.decoder(customJwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter()))
//                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
//        );
//        httpSecurity.csrf(AbstractHttpConfigurer::disable);
//        return httpSecurity.build();
//    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                                .defaultsDisabled() // Vô hiệu hóa toàn bộ headers mặc định
                                .cacheControl(cache -> {}) // (Tuỳ chọn) bạn có thể thêm header khác nếu muốn
                                .contentTypeOptions(contentType -> {}) // (Tuỳ chọn)
                        // Không thiết lập X-Frame-Options
                );

        return http.build();
    }
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return converter;
    }

}