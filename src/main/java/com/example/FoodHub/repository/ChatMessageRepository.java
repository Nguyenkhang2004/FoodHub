package com.example.FoodHub.repository;

import com.example.FoodHub.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    List<ChatMessage> findAllByTableId(Integer tableId);
}
