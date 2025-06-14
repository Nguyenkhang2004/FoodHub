package com.example.FoodHub.service;

import com.example.FoodHub.Specification.MenuItemSpecification;
import com.example.FoodHub.dto.request.MenuItemRequest;
import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.entity.Category;
import com.example.FoodHub.entity.MenuItem;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.repository.CategoryRepository;
import com.example.FoodHub.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class MenuItemService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public MenuItemService(MenuItemRepository menuItemRepository, CategoryRepository categoryRepository) {
        this.menuItemRepository = menuItemRepository;
        this.categoryRepository = categoryRepository;
    }

    //    public Page<MenuItemResponse> getMenuItems(Integer categoryId, String keyword, String sortBy, String sortDirection, int page, int size) {
//        String sortField = validateSortBy(sortBy);
//        boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
//        Pageable pageable = PageRequest.of(page, size);
//
//        String normalizedKeyword = (keyword != null) ? normalizeKeyword(keyword) : null;
//
//        Page<MenuItem> menuItems = menuItemRepository.findMenuItems(sortField, isAsc, categoryId, normalizedKeyword, pageable);
//        return menuItems.map(MenuItemResponse::new);
//    }
    public Page<MenuItemResponse> getMenuItems(Integer categoryId, String keyword, String status, String sortBy, String sortDirection, int page, int size) {
        // Validate page and size
        if (page < 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        // Validate status
        if (status != null && !status.isEmpty() && !Arrays.asList("AVAILABLE", "UNAVAILABLE").contains(status.toUpperCase())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        // Normalize inputs
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedStatus = (status != null && !status.isEmpty()) ? status.toUpperCase() : null;
        String sortField = validateSortBy(sortBy);
        boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

        // Build sort
        Sort sort = Sort.by(isAsc ? Sort.Direction.ASC : Sort.Direction.DESC, sortField).and(Sort.by("id"));
        Pageable pageable = PageRequest.of(page, size, sort);

        // Build specification
        Specification<MenuItem> spec = MenuItemSpecification.buildSpecification(categoryId, normalizedKeyword, normalizedStatus);

        // Execute query
        Page<MenuItem> menuItems = menuItemRepository.findAll(spec, pageable);
        return menuItems.map(MenuItemResponse::new);
    }

    private String validateSortBy(String sortBy) {
        return sortBy != null && Arrays.asList("name", "price", "status").contains(sortBy) ? sortBy : "name";
    }

    private boolean isValidSortField(String sortBy) {
        return Arrays.asList("id", "name", "price", "status").contains(sortBy);
    }

    private String normalizeKeyword(String keyword) {
        return keyword != null ? keyword.trim() : null;
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
        return new MenuItemResponse(menuItem);
    }

    @Transactional
    public MenuItemResponse createMenuItem(MenuItemRequest request) throws IOException {
        if (menuItemRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new AppException(ErrorCode.MENU_ITEM_EXISTED);
        }
        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setStatus("AVAILABLE");

        // Xử lý imageUrl
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            if (request.getImageUrl().startsWith("data:image")) {
                String fileName = saveBase64Image(request.getImageUrl());
                String imageUrl = "http://localhost:8080/foodhub/images/" + fileName + "?v=" + System.currentTimeMillis();
                menuItem.setImageUrl(imageUrl);
            } else {
                menuItem.setImageUrl(request.getImageUrl());
            }
        }
        // XỬ LÝ DANH MỤC TRƯỚC KHI LƯU
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            if (categories.size() != request.getCategoryIds().size()) {
                throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
            }
            // Thiết lập mối quan hệ ở MenuItem
            menuItem.setCategories(new LinkedHashSet<>(categories));
            // QUAN TRỌNG: Thiết lập mối quan hệ ngược lại ở Category
            for (Category category : categories) {
                category.getMenuItems().add(menuItem);
            }
        }

        // Lưu MenuItem (sẽ tự động lưu vào bảng menu_item_category)
        menuItem = menuItemRepository.save(menuItem);
        return new MenuItemResponse(menuItem);
    }
    @Transactional
    public MenuItemResponse updateMenuItem(Integer id, MenuItemRequest request) throws IOException {
        if (menuItemRepository.existsByNameIgnoreCaseAndIdNot(request.getName().trim(), id)) {
            throw new AppException(ErrorCode.MENU_ITEM_EXISTED);
        }
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        // Xử lý ảnh (chỉ khi có ảnh mới)
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            String newImageUrl = null;

            if (request.getImageUrl().startsWith("data:image")) {
                newImageUrl = "http://localhost:8080/foodhub/images/" + saveBase64Image(request.getImageUrl()) + "?v=" + System.currentTimeMillis();
            } else if (!request.getImageUrl().equals(menuItem.getImageUrl())) {
                newImageUrl = request.getImageUrl();
            }

            if (newImageUrl != null) {
                deleteOldImage(menuItem.getImageUrl());
                menuItem.setImageUrl(newImageUrl);
            }
        }

        // XỬ LÝ CẬP NHẬT DANH MỤC
        // Xóa tất cả quan hệ cũ ở cả hai phía
        for (Category oldCategory : menuItem.getCategories()) {
            oldCategory.getMenuItems().remove(menuItem);
        }
        menuItem.getCategories().clear();

        // Thiết lập quan hệ mới
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            if (categories.size() != request.getCategoryIds().size()) {
                throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
            }

            // Thiết lập mối quan hệ ở MenuItem
            menuItem.setCategories(new LinkedHashSet<>(categories));

            // QUAN TRỌNG: Thiết lập mối quan hệ ngược lại ở Category
            for (Category category : categories) {
                category.getMenuItems().add(menuItem);
            }
        }
        MenuItem updatedItem = menuItemRepository.save(menuItem);
        return new MenuItemResponse(updatedItem);
    }
    // Helper method để xóa ảnh cũ
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
        // Tạo thư mục nếu chưa tồn tại
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Failed to create directory: " + uploadDir);
            }
        }

        // Tách phần header (data:image/jpeg;base64,) và lấy dữ liệu base64
        String[] parts = base64Image.split(",");
        if (parts.length != 2) {
            throw new AppException(ErrorCode.INVALID_IMAGE_URL);
        }
        String imageData = parts[1];
        String extension = parts[0].split("/")[1].split(";")[0]; // Lấy định dạng (jpeg, png, ...)

        // Giải mã base64 thành byte[]
        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getDecoder().decode(imageData);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_IMAGE_URL);
        }

        // Tạo tên file duy nhất
        String fileName = UUID.randomUUID().toString() + "." + extension;
        String filePath = uploadDir + File.separator + fileName;

        // Lưu file vào thư mục
        Files.write(Paths.get(filePath), decodedBytes);

        return fileName;
    }

//    private String validateSortBy(String sortBy) {
//        if (!"name".equalsIgnoreCase(sortBy) && !"price".equalsIgnoreCase(sortBy)) {
//            return "name";
//        }
//        return sortBy.toLowerCase();
//    }
//
//    private String normalizeKeyword(String keyword) {
//        if (keyword == null) return null;
//        return keyword.toLowerCase().replaceAll("\\s+", " ");
//    }

    public long countMenuItems() {
        return menuItemRepository.count();
    }
}