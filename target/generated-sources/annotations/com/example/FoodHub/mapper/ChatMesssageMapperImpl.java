package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.ChatMessageRequest;
import com.example.FoodHub.dto.response.ChatMessageResponse;
import com.example.FoodHub.entity.ChatMessage;
import com.example.FoodHub.entity.RestaurantTable;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-16T07:22:11+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class ChatMesssageMapperImpl implements ChatMesssageMapper {

    @Override
    public ChatMessage toChatMessage(ChatMessageRequest request) {
        if ( request == null ) {
            return null;
        }

        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setSender( request.getSender() );
        chatMessage.setMessage( request.getMessage() );

        return chatMessage;
    }

    @Override
    public ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage) {
        if ( chatMessage == null ) {
            return null;
        }

        ChatMessageResponse.ChatMessageResponseBuilder chatMessageResponse = ChatMessageResponse.builder();

        chatMessageResponse.tableNumber( chatMessageTableTableNumber( chatMessage ) );
        chatMessageResponse.message( chatMessage.getMessage() );
        chatMessageResponse.timestamp( chatMessage.getTimestamp() );
        chatMessageResponse.sender( chatMessage.getSender() );

        return chatMessageResponse.build();
    }

    private String chatMessageTableTableNumber(ChatMessage chatMessage) {
        RestaurantTable table = chatMessage.getTable();
        if ( table == null ) {
            return null;
        }
        return table.getTableNumber();
    }
}
