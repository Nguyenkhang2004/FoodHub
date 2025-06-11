package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.MenuItemRequest;
import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.entity.Category;
import com.example.FoodHub.entity.MenuItem;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.MenuItemMapper;
import com.example.FoodHub.repository.CategoryRepository;
import com.example.FoodHub.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
public class MenuItemService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemMapper menuItemMapper;

    @Autowired
    public MenuItemService(MenuItemRepository menuItemRepository, CategoryRepository categoryRepository, MenuItemMapper menuItemMapper) {
        this.menuItemRepository = menuItemRepository;
        this.categoryRepository = categoryRepository;
        this.menuItemMapper = menuItemMapper;
    }

    public Page<MenuItemResponse> getMenuItems(Integer categoryId, String keyword, String sortBy, String sortDirection, int page, int size) {
        String sortField = validateSortBy(sortBy);
        boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
        Pageable pageable = PageRequest.of(page, size);

        String normalizedKeyword = (keyword != null) ? normalizeKeyword(keyword) : null;

        Page<MenuItem> menuItems = menuItemRepository.findMenuItems(sortField, isAsc, categoryId, normalizedKeyword, pageable);
        return menuItems.map(menuItemMapper::toMenuItemResponse);
    }

    @Transactional
    public void deleteMenuItem(Integer id) {
        if (!menuItemRepository.existsById(id)) {
            throw new AppException(ErrorCode.MENU_ITEM_NOT_FOUND);
        }

        int updatedRows = menuItemRepository.updateStatusById(id, "UNAVAILABLE");
        if (updatedRows == 0) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void restoreMenuItem(Integer id) {
        if (!menuItemRepository.existsById(id)) {
            throw new AppException(ErrorCode.MENU_ITEM_NOT_FOUND);
        }

        int updatedRows = menuItemRepository.updateStatusById(id, "AVAILABLE");
        if (updatedRows == 0) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public MenuItemResponse getMenuItemById(Integer id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));
        return menuItemMapper.toMenuItemResponse(menuItem);
    }

    @Transactional
    public MenuItemResponse createMenuItem(MenuItemRequest request) throws IOException {
        if (menuItemRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new AppException(ErrorCode.MENU_ITEM_EXISTED);
        }

        MenuItem menuItem = menuItemMapper.toMenuItem(request);

        // Xử lý imageUrl
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            if (request.getImageUrl().startsWith("data:image")) {
                String fileName = saveBase64Image(request.getImageUrl());
                String imageUrl = "/foodhub/images/" + fileName + "?v=" + System.currentTimeMillis();
                menuItem.setImageUrl(imageUrl);
            } else {
                menuItem.setImageUrl(request.getImageUrl());
            }
        }

        // Xử lý danh mục
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            if (categories.size() != request.getCategoryIds().size()) {
                throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
            }
            menuItem.setCategories(new LinkedHashSet<>(categories));
            for (Category category : categories) {
                category.getMenuItems().add(menuItem);
            }
        }

        MenuItem savedItem = menuItemRepository.save(menuItem);
        return menuItemMapper.toMenuItemResponse(savedItem);
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Integer id, MenuItemRequest request) throws IOException {
        if (menuItemRepository.existsByNameIgnoreCaseAndIdNot(request.getName().trim(), id)) {
            throw new AppException(ErrorCode.MENU_ITEM_EXISTED);
        }

        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));

        menuItemMapper.updateMenuItemFromRequest(request, menuItem);

        // Xử lý ảnh
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            String newImageUrl = null;
            if (request.getImageUrl().startsWith("data:image")) {
                newImageUrl = "/foodhub/images/" + saveBase64Image(request.getImageUrl()) + "?v=" + System.currentTimeMillis();
            } else if (!request.getImageUrl().equals(menuItem.getImageUrl())) {
                newImageUrl = request.getImageUrl();
            }

            if (newImageUrl != null) {
                deleteOldImage(menuItem.getImageUrl());
                menuItem.setImageUrl(newImageUrl);
            }
        }

        // Xử lý danh mục
        for (Category oldCategory : menuItem.getCategories()) {
            oldCategory.getMenuItems().remove(menuItem);
        }
        menuItem.getCategories().clear();

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            if (categories.size() != request.getCategoryIds().size()) {
                throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
            }
            menuItem.setCategories(new LinkedHashSet<>(categories));
            for (Category category : categories) {
                category.getMenuItems().add(menuItem);
            }
        }

        MenuItem updatedItem = menuItemRepository.save(menuItem);
        return menuItemMapper.toMenuItemResponse(updatedItem);
    }

    private void deleteOldImage(String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith("/foodhub/images/")) {
            String fileName = imageUrl.split("\\?")[0].replace("/foodhub/images/", "");
            File oldFile = new File(uploadDir + File.separator + fileName);
            if (oldFile.exists() && !oldFile.delete()) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }
    }

    private String saveBase64Image(String base64Image) throws IOException {
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Failed to create directory: " + uploadDir);
            }
        }

        String[] parts = base64Image.split(",");
        if (parts.length != 2) {
            throw new AppException(ErrorCode.INVALID_IMAGE_URL);
        }
        String imageData = parts[1];
        String extension = parts[0].split("/")[1].split(";")[0];

        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getDecoder().decode(imageData);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_IMAGE_URL);
        }

        String fileName = UUID.randomUUID().toString() + "." + extension;
        String filePath = uploadDir + File.separator + fileName;
        Files.write(Paths.get(filePath), decodedBytes);

        return fileName;
    }

    private String validateSortBy(String sortBy) {
        if (!"name".equalsIgnoreCase(sortBy) && !"price".equalsIgnoreCase(sortBy)) {
            return "name";
        }
        return sortBy.toLowerCase();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        return keyword.toLowerCase().replaceAll("\\s+", " ");
    }

    public long countMenuItems() {
        return menuItemRepository.count();
    }
}