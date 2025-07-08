package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.ChatMessageRequest;
import com.example.FoodHub.dto.request.ChatRequest;
import com.example.FoodHub.dto.response.ChatMessageResponse;
import com.example.FoodHub.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatRealTimeController {
    private final ChatService chatService;

    @MessageMapping("/chat/table/{tableNumber}")
    public void chat(@DestinationVariable String tableNumber, @Payload ChatMessageRequest request) {
        try{
            log.info("Received chat message for table in controller: {}, sender: {}, message: {}", tableNumber, request.getSender(), request.getMessage());
            chatService.sendMessage(tableNumber, request);
        }catch (Exception e) {
            log.error("Error processing chat message for table {}: {}", tableNumber, e.getMessage());
            // Handle the error appropriately, e.g., send an error response back to the client
        }

    }

    @GetMapping("/chat/messages/table/{tableNumber}")
    public ResponseEntity<List<ChatMessageResponse>> getChatMessages(@PathVariable String tableNumber) {
        log.info("Fetching chat messages for table: {}", tableNumber);
        List<ChatMessageResponse> messages = chatService.getChatMessagesByTableNumber(tableNumber);
        return ResponseEntity.ok(messages);
    }

}
