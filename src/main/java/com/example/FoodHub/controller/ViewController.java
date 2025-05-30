package com.example.FoodHub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @GetMapping("/login")
    public String getLoginPage() {
        // This method returns the view name for the login page
        return "login"; // Assuming you have a Thymeleaf template named 'login.html'
    }
}
