package com.example.FoodHub.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/waiter")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WaiterController {

    @GetMapping
    public String getWaiterPage() {
        // This method returns the view name for the waiter page
        return "waiter/index"; // Assuming you have a Thymeleaf template named 'index.html'
    }
}
