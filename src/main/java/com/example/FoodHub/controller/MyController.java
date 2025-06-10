package com.example.FoodHub.controller;

import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://127.0.0.1:5500")
@RestController
public class MyController {

    @GetMapping("/api/hello")
    public String sayHello() {
        return "Xin chào từ Spring Boot!";
    }
}