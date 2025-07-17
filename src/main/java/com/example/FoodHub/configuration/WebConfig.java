package com.example.FoodHub.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://127.0.0.1:5500",
                                "http://10.12.48.109:5500",
                                "http://172.20.10.2:5500",
                                "http://192.168.1.6:5500",
                                "http://172.20.10.7:5500")

                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true); // Cho phép gửi token/cookie
            }

            // Cấu hình phục vụ file PDF tĩnh từ thư mục invoices/
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/invoices/**")
                        .addResourceLocations("file:invoices/");
            }
        };
    }
}