package com.example.FoodHub.service;

import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.mapper.MenuItemMapper;
import com.example.FoodHub.repository.MenuItemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Page;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
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

    public List<MenuItemResponse> getMenuItemsByCategory(String categoryName) {
        log.info("Fetching menu items for category: {}", categoryName);
        return menuItemRepository.findByCategoriesNameIgnoreCase(categoryName).stream()
                .map(menuItemMapper::toMenuItemResponse)
                .toList();
    }

    public List<MenuItemResponse> getAvailableMenuItems() {
        log.info("Fetching AVAILABLE menu items");
        return menuItemRepository.findByStatusIgnoreCase("AVAILABLE").stream()
                .map(menuItemMapper::toMenuItemResponse)
                .toList();
    }

    public MenuItemResponse getMenuItemById(Integer id) {
        return menuItemRepository.findById(id)
                .map(menuItemMapper::toMenuItemResponse)
                .orElse(null);
    }



}
