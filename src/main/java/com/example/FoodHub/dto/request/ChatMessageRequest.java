package com.example.FoodHub.dto.request;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private String message;
    private String sender;
}
