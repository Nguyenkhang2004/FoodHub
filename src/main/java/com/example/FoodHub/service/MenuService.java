package com.example.FoodHub.service;

import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.mapper.MenuItemMapper;
import com.example.FoodHub.repository.MenuItemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MenuService {
    MenuItemRepository menuItemRepository;
    MenuItemMapper menuItemMapper;

    public Page<MenuItemResponse> getAllMenuItems(Pageable pageable) {
        log.info("Fetching paginated menu items");
        return menuItemRepository.findAll(pageable).map(menuItemMapper::toMenuItemResponse);
    }

    public Page<MenuItemResponse> getMenuItemsByCategory(String categoryName, Pageable pageable) {
        log.info("Fetching paginated menu items for category: {}", categoryName);
        return menuItemRepository.findByCategoriesNameIgnoreCase(categoryName, pageable)
                .map(menuItemMapper::toMenuItemResponse);
    }

    public Page<MenuItemResponse> getAvailableMenuItems(Pageable pageable) {
        log.info("Fetching paginated AVAILABLE menu items");
        return menuItemRepository.findByStatusIgnoreCase("AVAILABLE", pageable)
                .map(menuItemMapper::toMenuItemResponse);
    }
}