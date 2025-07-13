package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.ChatMessageRequest;
import com.example.FoodHub.dto.request.ChatRequest;
import com.example.FoodHub.dto.response.ChatMessageResponse;
import com.example.FoodHub.entity.ChatMessage;
import com.example.FoodHub.entity.RestaurantTable;
import com.example.FoodHub.enums.Role;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.ChatMesssageMapper;
import com.example.FoodHub.repository.ChatMessageRepository;
import com.example.FoodHub.repository.RestaurantTableRepository;
import com.example.FoodHub.utils.TimeUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatService {
    SimpMessagingTemplate simpMessagingTemplate;
    RestaurantTableRepository tableRepository;
    ChatMessageRepository chatMessageRepository;
    ChatMesssageMapper chatMesssageMapper;

//    @PreAuthorize("hasAuthority('ACCESS_CHAT')")
    public void sendMessage(String tableNumber, ChatMessageRequest request) {
        log.info("Received chat request for table: {}, sender: {}, message: {}", tableNumber, request.getSender(), request.getMessage());
        ChatMessage message = chatMesssageMapper.toChatMessage(request);
        RestaurantTable table = tableRepository.findByTableNumber(tableNumber)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
        message.setTable(table);
        String topic = "";
        if(request.getSender().equals(Role.CUSTOMER.name())) {
            topic = "/topic/chat/waiter/area/" + table.getArea();
        } else if(request.getSender().equals(Role.WAITER.name())) {
            topic = "/topic/chat/customer/table/" + tableNumber;
        } else {
            log.error("Unknown sender type: {}", request.getSender());
            return;
        }
        ChatMessageResponse messageResponse = chatMesssageMapper.toChatMessageResponse(chatMessageRepository.save(message));
        log.info("Sending message to topic: {}", topic);
        simpMessagingTemplate.convertAndSend(topic, messageResponse);
    }

//    @PreAuthorize("hasAuthority('ACCESS_CHAT')")
    public List<ChatMessageResponse> getChatMessagesByTableNumber(String tableNumber) {
        log.info("Fetching chat messages for table: {}", tableNumber);
        RestaurantTable table = tableRepository.findByTableNumber(tableNumber)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
        List<ChatMessage> messages = chatMessageRepository.findAllByTableId(table.getId());
        return messages.stream()
                .map(chatMesssageMapper::toChatMessageResponse)
                .collect(Collectors.toList());
    }
}
