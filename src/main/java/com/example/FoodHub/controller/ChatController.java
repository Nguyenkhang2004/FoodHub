package com.example.FoodHub.controller;



import com.example.FoodHub.dto.request.ChatRequest;
import com.example.FoodHub.dto.response.ChatResponse;
import com.example.FoodHub.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://127.0.0.1:5500")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final GeminiService geminiService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String reply = geminiService.generateReply(request.getMessage());
        return ResponseEntity.ok(new ChatResponse(reply));
    }
}
