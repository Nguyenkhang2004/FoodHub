package com.example.FoodHub.controller;

import com.example.FoodHub.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.MessagingException;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "http://127.0.0.1:5500") // Cho phép CORS nếu cần (tùy môi trường)
public class FeedbackController {

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<String> submitFeedback(@RequestBody Map<String, String> feedback) {
        try {
            String username = feedback.getOrDefault("username", "Khách");
            String email = feedback.get("email");
            String message = feedback.get("message");

            if (email == null || message == null) {
                return ResponseEntity.badRequest().body("Email và nội dung phản hồi là bắt buộc");
            }

            emailService.sendFeedbackEmailAsync(email, username, message);
            return ResponseEntity.ok("Phản hồi đã được gửi thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi gửi phản hồi: " + e.getMessage());
        }
    }
}