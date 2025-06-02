package com.example.FoodHub.service;
 // Đảm bảo import đúng DTO
import com.example.FoodHub.dto.request.MenuItemRequest;
import com.example.FoodHub.dto.response.MenuItemreponse;
import com.example.FoodHub.entity.Category;
import com.example.FoodHub.entity.MenuItem;
import com.example.FoodHub.repository.CategoryRepository;
import com.example.FoodHub.repository.MenuItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page; // Import từ Spring Data
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
public class MenuItemService {

//    @Autowired
//    private MenuItemRepository menuItemRepository;
//
//    public Page<MenuItemreponse> getMenuItems(Integer categoryId, String sortBy,String keyword, String sortDirection, int page, int size) {
//        String sortField = validateSortBy(sortBy);
//        boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
//        Pageable pageable = PageRequest.of(page, size);
//
//        Page<MenuItem> menuItems;
//        if (categoryId != null) {
//            menuItems = menuItemRepository.findByCategoryIdOrderBy(sortField, isAsc, categoryId,keyword,pageable);
//        } else {
//            menuItems = menuItemRepository.findAllOrderBy(sortField, isAsc, keyword,pageable);
//        }
//
//        return menuItems.map(MenuItemreponse::new); // Sử dụng map() từ Spring Data Page
//    }
@Autowired
private MenuItemRepository menuItemRepository;
@Autowired
private CategoryRepository categoryRepository;
@Value("${app.upload.dir}")
private String uploadDir;
    public Page<MenuItemreponse> getMenuItems(Integer categoryId, String keyword, String sortBy, String sortDirection, int page, int size) {
        String sortField = validateSortBy(sortBy);
        boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
        Pageable pageable = PageRequest.of(page, size);

        String normalizedKeyword = (keyword != null) ? normalizeKeyword(keyword) : null;

        Page<MenuItem> menuItems = menuItemRepository.findMenuItems(sortField, isAsc, categoryId, normalizedKeyword, pageable);
        return menuItems.map(MenuItemreponse::new); //
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
    @Transactional
    public boolean deleteMenuItem(Integer id) {
        try {
            if (!menuItemRepository.existsById(id)) {
                return false; // MenuItem không tồn tại
            }

            int updatedRows = menuItemRepository.updateStatusById(id, "UNAVAILABLE");
            return updatedRows > 0;
        } catch (Exception e) {
            System.err.println("Error deleting menu item: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean restoreMenuItem(Integer id) {
        try {
            if (!menuItemRepository.existsById(id)) {
                return false; // MenuItem không tồn tại
            }

            int updatedRows = menuItemRepository.updateStatusById(id, "AVAILABLE");
            return updatedRows > 0;
        } catch (Exception e) {
            System.err.println("Error restoring menu item: " + e.getMessage());
            return false;
        }
    }

    public MenuItemreponse getMenuItemById(Integer id) {
        return menuItemRepository.findById(id)
                .map(MenuItemreponse::new)
                .orElse(null);
    }
    public MenuItemreponse createMenuItem(MenuItemRequest request) throws IOException {
        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setStatus("AVAILABLE");

        // Xử lý imageUrl thay vì MultipartFile
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            // Nếu là base64, lưu file
            if (request.getImageUrl().startsWith("data:image")) {
                String fileName = saveBase64Image(request.getImageUrl(), uploadDir);
                String imageUrl = "/foodhub/images/" + fileName + "?v=" + System.currentTimeMillis();
                menuItem.setImageUrl(imageUrl);
            } else {
                // Nếu là URL thông thường
                menuItem.setImageUrl(request.getImageUrl());
            }
        }

        // Lưu MenuItem trước để đảm bảo nó không còn là transient
        menuItem = menuItemRepository.save(menuItem);

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            menuItem.setCategories(new LinkedHashSet<>(categories));
            for (Category category : categories) {
                category.getMenuItems().add(menuItem);
            }
            // Lưu các category sau khi MenuItem đã được lưu
            categoryRepository.saveAll(categories);
        }


        return new MenuItemreponse(menuItem);
    }
    @Transactional
    public MenuItemreponse updateMenuItem(Integer id, MenuItemRequest request) throws IOException {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại với ID: " + id));

        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());

        // Xử lý ảnh base64 (nếu có)
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            // Xóa ảnh cũ nếu tồn tại
            if (menuItem.getImageUrl() != null && !menuItem.getImageUrl().isEmpty()) {
                String oldFileName = menuItem.getImageUrl().replace("/foodhub/images/", "");

            }

            // Lưu ảnh mới
            if (request.getImageUrl().startsWith("data:image")) {
                String fileName = saveBase64Image(request.getImageUrl(), uploadDir);
                String imageUrl = "/foodhub/images/" + fileName + "?v=" + System.currentTimeMillis();
                menuItem.setImageUrl(imageUrl);
            } else {
                menuItem.setImageUrl(request.getImageUrl());
            }
        }

        // Cập nhật danh mục
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            menuItem.getCategories().clear();
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            menuItem.setCategories(new LinkedHashSet<>(categories));
            for (Category category : categories) {
                category.getMenuItems().add(menuItem);
            }
            categoryRepository.saveAll(categories);
        }

        MenuItem updatedItem = menuItemRepository.save(menuItem);
        return new MenuItemreponse(updatedItem);
    }

    // 4. Thêm method helper để xử lý base64 image
    public String saveBase64Image(String base64Data, String uploadDir) throws IOException {
        String[] parts = base64Data.split(",");
        String metadata = parts[0];
        String data = parts[1];

        String extension = metadata.contains("jpeg") ? ".jpg" : metadata.contains("png") ? ".png" : ".img";
        String fileName = UUID.randomUUID() + extension;

        // Tạo thư mục nếu chưa tồn tại
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        byte[] imageBytes = Base64.getDecoder().decode(data);
        Path path = Paths.get(uploadDir, fileName);
        Files.write(path, imageBytes);

        return fileName;
    }

}

//    private String validateSortBy(String sortBy) {
//        if (!"name".equalsIgnoreCase(sortBy) && !"price".equalsIgnoreCase(sortBy)) {
//            return "name";
//        }
//        return sortBy.toLowerCase();
//    }
