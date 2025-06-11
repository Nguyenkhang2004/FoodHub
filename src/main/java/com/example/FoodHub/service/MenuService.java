package com.example.FoodHub.service;

import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.mapper.MenuItemMapper;
import com.example.FoodHub.repository.MenuItemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MenuService {
    MenuItemRepository menuItemRepository;
    MenuItemMapper menuItemMapper;
    public List<MenuItemResponse> getAllMenuItems() {
        log.info("Fetching all menu items");
        return menuItemRepository.findAll().stream()
                .map(menuItemMapper::toMenuItemResponse)
                .toList();
    }
}
