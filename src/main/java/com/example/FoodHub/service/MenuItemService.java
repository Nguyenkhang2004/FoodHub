//package com.example.FoodHub.service;
// // Đảm bảo import đúng DTO
//import com.example.FoodHub.dto.request.MenuItemRequest;
//import com.example.FoodHub.dto.response.MenuItemreponse;
//import com.example.FoodHub.entity.Category;
//import com.example.FoodHub.entity.MenuItem;
//import com.example.FoodHub.repository.CategoryRepository;
//import com.example.FoodHub.repository.MenuItemRepository;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.domain.Page; // Import từ Spring Data
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Base64;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//public class MenuItemService {
//
////    @Autowired
////    private MenuItemRepository menuItemRepository;
////
////    public Page<MenuItemreponse> getMenuItems(Integer categoryId, String sortBy,String keyword, String sortDirection, int page, int size) {
////        String sortField = validateSortBy(sortBy);
////        boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
////        Pageable pageable = PageRequest.of(page, size);
////
////        Page<MenuItem> menuItems;
////        if (categoryId != null) {
////            menuItems = menuItemRepository.findByCategoryIdOrderBy(sortField, isAsc, categoryId,keyword,pageable);
////        } else {
////            menuItems = menuItemRepository.findAllOrderBy(sortField, isAsc, keyword,pageable);
////        }
////
////        return menuItems.map(MenuItemreponse::new); // Sử dụng map() từ Spring Data Page
////    }
//@Autowired
//private MenuItemRepository menuItemRepository;
//@Autowired
//private CategoryRepository categoryRepository;
//@Value("${app.upload.dir}")
//private String uploadDir;
//    public Page<MenuItemreponse> getMenuItems(Integer categoryId, String keyword, String sortBy, String sortDirection, int page, int size) {
//        String sortField = validateSortBy(sortBy);
//        boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
//        Pageable pageable = PageRequest.of(page, size);
//
//        String normalizedKeyword = (keyword != null) ? normalizeKeyword(keyword) : null;
//
//        Page<MenuItem> menuItems = menuItemRepository.findMenuItems(sortField, isAsc, categoryId, normalizedKeyword, pageable);
//        return menuItems.map(MenuItemreponse::new); //
//    }
//
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
//    @Transactional
//    public boolean deleteMenuItem(Integer id) {
//        try {
//            if (!menuItemRepository.existsById(id)) {
//                return false; // MenuItem không tồn tại
//            }
//
//            int updatedRows = menuItemRepository.updateStatusById(id, "UNAVAILABLE");
//            return updatedRows > 0;
//        } catch (Exception e) {
//            System.err.println("Error deleting menu item: " + e.getMessage());
//            return false;
//        }
//    }
//
//    @Transactional
//    public boolean restoreMenuItem(Integer id) {
//        try {
//            if (!menuItemRepository.existsById(id)) {
//                return false; // MenuItem không tồn tại
//            }
//
//            int updatedRows = menuItemRepository.updateStatusById(id, "AVAILABLE");
//            return updatedRows > 0;
//        } catch (Exception e) {
//            System.err.println("Error restoring menu item: " + e.getMessage());
//            return false;
//        }
//    }
//
//    public MenuItemreponse getMenuItemById(Integer id) {
//        return menuItemRepository.findById(id)
//                .map(MenuItemreponse::new)
//                .orElse(null);
//    }
//    public MenuItemreponse createMenuItem(MenuItemRequest request) throws IOException {
//        MenuItem menuItem = new MenuItem();
//        menuItem.setName(request.getName());
//        menuItem.setDescription(request.getDescription());
//        menuItem.setPrice(request.getPrice());
//        menuItem.setStatus("AVAILABLE");
//
//        // Xử lý imageUrl thay vì MultipartFile
//        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
//            // Nếu là base64, lưu file
//            if (request.getImageUrl().startsWith("data:image")) {
//                String fileName = saveBase64Image(request.getImageUrl(), uploadDir);
//                String imageUrl = "/foodhub/images/" + fileName + "?v=" + System.currentTimeMillis();
//                menuItem.setImageUrl(imageUrl);
//            } else {
//                // Nếu là URL thông thường
//                menuItem.setImageUrl(request.getImageUrl());
//            }
//        }
//
//        // Lưu MenuItem trước để đảm bảo nó không còn là transient
//        menuItem = menuItemRepository.save(menuItem);
//
//        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
//            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
//            menuItem.setCategories(new LinkedHashSet<>(categories));
//            for (Category category : categories) {
//                category.getMenuItems().add(menuItem);
//            }
//            // Lưu các category sau khi MenuItem đã được lưu
//            categoryRepository.saveAll(categories);
//        }
//
//
//        return new MenuItemreponse(menuItem);
//    }
//    @Transactional
//    public MenuItemreponse updateMenuItem(Integer id, MenuItemRequest request) throws IOException {
//        MenuItem menuItem = menuItemRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại với ID: " + id));
//
//        menuItem.setName(request.getName());
//        menuItem.setDescription(request.getDescription());
//        menuItem.setPrice(request.getPrice());
//
//        // Xử lý ảnh base64 (nếu có)
//        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
//            // Xóa ảnh cũ nếu tồn tại
//            if (menuItem.getImageUrl() != null && !menuItem.getImageUrl().isEmpty()) {
//                String oldFileName = menuItem.getImageUrl().replace("/foodhub/images/", "");
//
//            }
//
//            // Lưu ảnh mới
//            if (request.getImageUrl().startsWith("data:image")) {
//                String fileName = saveBase64Image(request.getImageUrl(), uploadDir);
//                String imageUrl = "/foodhub/images/" + fileName + "?v=" + System.currentTimeMillis();
//                menuItem.setImageUrl(imageUrl);
//            } else {
//                menuItem.setImageUrl(request.getImageUrl());
//            }
//        }
//
//        // Cập nhật danh mục
//        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
//            menuItem.getCategories().clear();
//            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
//            menuItem.setCategories(new LinkedHashSet<>(categories));
//            for (Category category : categories) {
//                category.getMenuItems().add(menuItem);
//            }
//            categoryRepository.saveAll(categories);
//        }
//
//        MenuItem updatedItem = menuItemRepository.save(menuItem);
//        return new MenuItemreponse(updatedItem);
//    }
//
//    // 4. Thêm method helper để xử lý base64 image
//    public String saveBase64Image(String base64Data, String uploadDir) throws IOException {
//        String[] parts = base64Data.split(",");
//        String metadata = parts[0];
//        String data = parts[1];
//
//        String extension = metadata.contains("jpeg") ? ".jpg" : metadata.contains("png") ? ".png" : ".img";
//        String fileName = UUID.randomUUID() + extension;
//
//        // Tạo thư mục nếu chưa tồn tại
//        File dir = new File(uploadDir);
//        if (!dir.exists()) dir.mkdirs();
//
//        byte[] imageBytes = Base64.getDecoder().decode(data);
//        Path path = Paths.get(uploadDir, fileName);
//        Files.write(path, imageBytes);
//
//        return fileName;
//    }
//
//}
//
////    private String validateSortBy(String sortBy) {
////        if (!"name".equalsIgnoreCase(sortBy) && !"price".equalsIgnoreCase(sortBy)) {
////            return "name";
////        }
////        return sortBy.toLowerCase();
////    }

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
import com.example.FoodHub.specification.MenuItemSpecification;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemMapper menuItemMapper;
    @Value("${app.upload.dir}")
    private String uploadDir;

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;



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
        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setStatus("AVAILABLE");

        // Xử lý imageUrl
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            if (request.getImageUrl().startsWith("data:image")) {
                String fileName = saveBase64Image(request.getImageUrl());
                String imageUrl = "http://localhost:8080/images/" + fileName + "?v=" + System.currentTimeMillis();
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
                newImageUrl = "http://localhost:8080/images/" + saveBase64Image(request.getImageUrl()) + "?v=" + System.currentTimeMillis();
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