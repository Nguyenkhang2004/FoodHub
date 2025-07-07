package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.ChatMessageRequest;
import com.example.FoodHub.dto.response.ChatMessageResponse;
import com.example.FoodHub.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMesssageMapper {
    ChatMessage toChatMessage(ChatMessageRequest request);
    @Mapping(source = "table.tableNumber", target = "tableNumber")
    ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage);
}
