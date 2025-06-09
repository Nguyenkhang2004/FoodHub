package com.example.FoodHub.configuration;



import com.example.FoodHub.dto.request.ChatRequest;
import com.example.FoodHub.dto.response.ChatResponse;
import com.example.FoodHub.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final GeminiService geminiService;

    public ChatController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String reply = geminiService.generateReply(request.getMessage());
        return ResponseEntity.ok(new ChatResponse(reply));
    }
}
