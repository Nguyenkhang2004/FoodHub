package com.example.FoodHub.controller;



import com.example.FoodHub.dto.request.ChatRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.ChatResponse;
import com.example.FoodHub.service.ChatService;
import com.example.FoodHub.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
@Slf4j
@CrossOrigin(origins = "http://127.0.0.1:5500")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final GeminiService geminiService;
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chatAI(@RequestBody ChatRequest request) {
        String reply = geminiService.generateReply(request.getMessage());
        return ResponseEntity.ok(new ChatResponse(reply));
    }



}
