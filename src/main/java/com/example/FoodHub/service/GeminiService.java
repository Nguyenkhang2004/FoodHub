package com.example.FoodHub.service;

import com.example.FoodHub.entity.MenuItem;
import com.example.FoodHub.repository.MenuItemRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GeminiService {
    @Autowired
    private final MenuItemRepository menuItemRepository;

    private static final String API_KEY = "AIzaSyCHLZsfZyROL8zEN6Mc9yVkPqwePEUCLnc";
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    private final Map<String, Function<String, String>> handlers = new HashMap<>();

    public GeminiService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;

        handlers.put("món đắt nhất", this::handleMostExpensive);
        handlers.put("món rẻ nhất", this::handleCheapest);
        handlers.put("món nước đắt nhất", this::handleMostExpensiveDrink);
        handlers.put("món nước rẻ nhất", this::handleCheapestDrink);
        handlers.put("món chay đắt nhất", this::handleMostExpensiveVegetarian);
        handlers.put("món chay rẻ nhất", this::handleCheapestVegetarian);
        handlers.put("món trong ngân sách", this::handleWithinBudget);
        handlers.put("món lẩu đắt nhất", msg -> handleMostExpensiveByCategory("Lẩu"));
        handlers.put("món nướng rẻ nhất", msg -> handleCheapestByCategory("Nướng"));
        handlers.put("khai vị đắt nhất", msg -> handleMostExpensiveByCategory("Khai vị"));
        handlers.put("đồ uống rẻ nhất", msg -> handleCheapestByCategory("Đồ uống"));
        handlers.put("combo ngon nhất", msg -> handleComboRecommendation());

    }

    public String generateReply(String userMessage) {
        String lower = userMessage.toLowerCase();

        for (Map.Entry<String, Function<String, String>> entry : handlers.entrySet()) {
            if (lower.contains(entry.getKey())) {
                String rawResponse = entry.getValue().apply(userMessage);
                return formatResponse(rawResponse);
            }
        }

        List<MenuItem> allItems = menuItemRepository.findAll();

        String menuInfo = allItems.stream()
                .map(i -> "- " + i.getName() + " (giá: " + i.getPrice() + "đ)")
                .collect(Collectors.joining("\n"));

        String prompt = """
        %s
        
        Dựa vào thực đơn sau, hãy tư vấn món ăn phù hợp:
        %s
        """.formatted(userMessage, menuInfo);

        return callGeminiAPI(prompt);
    }

    private String handleMostExpensive(String msg) {
        List<MenuItem> items = menuItemRepository.findAll();
        return items.stream()
                .max(Comparator.comparing(MenuItem::getPrice))
                .map(i -> "Món đắt nhất hiện tại là *" + i.getName() + "* với giá " + i.getPrice() + "đ.")
                .orElse("Không tìm thấy món đắt nhất.");
    }

    private String handleCheapest(String msg) {
        List<MenuItem> items = menuItemRepository.findAll();
        return items.stream()
                .min(Comparator.comparing(MenuItem::getPrice))
                .map(i -> "Món rẻ nhất hiện tại là *" + i.getName() + "* với giá " + i.getPrice() + "đ.")
                .orElse("Không tìm thấy món rẻ nhất.");
    }

    private String handleMostExpensiveDrink(String msg) {
        List<MenuItem> drinks = filterByCategory("Đồ uống");
        return drinks.stream()
                .max(Comparator.comparing(MenuItem::getPrice))
                .map(i -> "Món nước đắt nhất là *" + i.getName() + "* với giá " + i.getPrice() + "đ.")
                .orElse("Không có món nước nào.");
    }

    private String handleCheapestDrink(String msg) {
        List<MenuItem> drinks = filterByCategory("Đồ uống");
        return drinks.stream()
                .min(Comparator.comparing(MenuItem::getPrice))
                .map(i -> "Món nước rẻ nhất là *" + i.getName() + "* với giá " + i.getPrice() + "đ.")
                .orElse("Không có món nước nào.");
    }

    private String handleMostExpensiveVegetarian(String msg) {
        List<MenuItem> vegs = filterByVegetarian(true);
        return vegs.stream()
                .max(Comparator.comparing(MenuItem::getPrice))
                .map(i -> "Món chay đắt nhất là *" + i.getName() + "* với giá " + i.getPrice() + "đ.")
                .orElse("Không có món chay nào.");
    }

    private String handleCheapestVegetarian(String msg) {
        List<MenuItem> vegs = filterByVegetarian(true);
        return vegs.stream()
                .min(Comparator.comparing(MenuItem::getPrice))
                .map(i -> "Món chay rẻ nhất là *" + i.getName() + "* với giá " + i.getPrice() + "đ.")
                .orElse("Không có món chay nào.");
    }

    private String handleWithinBudget(String msg) {
        int budget = extractBudgetFromMessage(msg);
        List<MenuItem> allItems = menuItemRepository.findAll();
        List<MenuItem> suitable = allItems.stream()
                .filter(i -> i.getPrice().intValue() <= budget)
                .collect(Collectors.toList());

        if (suitable.isEmpty()) {
            return "Không có món nào phù hợp với ngân sách " + budget + "đ.";
        }
        String list = suitable.stream()
                .map(i -> "- " + i.getName() + " (giá: " + i.getPrice() + "đ)")
                .collect(Collectors.joining("\n"));
        return "Các món phù hợp với ngân sách " + budget + "đ:\n" + list;
    }

    private String handleMostExpensiveByCategory(String categoryName) {
        List<MenuItem> items = filterByCategory(categoryName);
        return items.stream()
                .max(Comparator.comparing(MenuItem::getPrice))
                .map(i -> "Món " + categoryName.toLowerCase() + " đắt nhất là :" + i.getName() + "\nvới giá " + i.getPrice() + "đ.")
                .orElse("Không có món " + categoryName.toLowerCase() + " nào.");
    }

    private String handleCheapestByCategory(String categoryName) {
        List<MenuItem> items = filterByCategory(categoryName);
        return items.stream()
                .min(Comparator.comparing(MenuItem::getPrice))
                .map(i -> "Món " + categoryName.toLowerCase() + " rẻ nhất là *" + i.getName() + "* với giá " + i.getPrice() + "đ.")
                .orElse("Không có món " + categoryName.toLowerCase() + " nào.");
    }

    private List<MenuItem> filterByCategory(String categoryName) {
        List<MenuItem> allItems = menuItemRepository.findAll();
        return allItems.stream()
                .filter(item -> item.getCategories().stream()
                        .anyMatch(cat -> cat.getName().equalsIgnoreCase(categoryName)))
                .collect(Collectors.toList());
    }

    private List<MenuItem> filterByVegetarian(boolean vegetarian) {
        List<MenuItem> allItems = menuItemRepository.findAll();
        return allItems.stream()
                .filter(item -> vegetarian ? "VEGETARIAN".equalsIgnoreCase(item.getStatus()) : true)
                .collect(Collectors.toList());
    }

    private int extractBudgetFromMessage(String message) {
        String[] parts = message.split("\\D+");
        for (String part : parts) {
            if (!part.isEmpty()) {
                try {
                    return Integer.parseInt(part);
                } catch (NumberFormatException ignored) {}
            }
        }
        return Integer.MAX_VALUE;
    }

    private String handleComboRecommendation() {
        return "Combo ngon nhất hiện tại gồm Lẩu Thái Hải Sản và Ba Chỉ Bò Mỹ Nướng. Giá hợp lý và hương vị tuyệt vời!";
    }

    private String formatResponse(String rawText) {
        String prompt = "Viết lại đoạn sau theo phong cách trò chuyện, thêm emoji và định dạng Markdown:\n" + rawText;
        return callGeminiAPI(prompt);
    }

    private String callGeminiAPI(String prompt) {
        try {
            String json = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "%s"
                    }
                  ]
                }
              ]
            }
            """.formatted(prompt);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject jsonObj = new JSONObject(response.body());

            if (!jsonObj.has("candidates")) {
                return "Gemini không trả về kết quả hợp lệ.";
            }

            return jsonObj
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

        } catch (Exception e) {
            e.printStackTrace();
            return "Đã xảy ra lỗi khi gọi Gemini API.";
        }
    }
}
