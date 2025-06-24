package com.example.FoodHub.service;

import com.example.FoodHub.entity.MenuItem;
import com.example.FoodHub.repository.MenuItemRepository;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    private final MenuItemRepository menuItemRepository;

    private static final String API_KEY = "AIzaSyCHLZsfZyROL8zEN6Mc9yVkPqwePEUCLnc";
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    private final Map<String, String> categoryAlias = Map.of(
            "lẩu", "Lẩu nước",
            "nướng thịt", "Món nướng thịt",
            "nướng rau", "Món nướng rau củ",
            "khai vị", "Món khai vị",
            "đồ uống", "Đồ uống không cồn"
    );

    private final Map<String, Function<String, String>> handlers = new HashMap<>();

    public GeminiService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
        initHandlers();
    }

    private void initHandlers() {
        handlers.put("món đắt nhất", msg -> respond(menuItemRepository.findTopByOrderByPriceDesc(), "Món đắt nhất hiện tại là"));
        handlers.put("món rẻ nhất", msg -> respond(menuItemRepository.findTopByOrderByPriceAsc(), "Món rẻ nhất hiện tại là"));
        handlers.put("món chay đắt nhất", msg -> respond(menuItemRepository.findTopByStatusIgnoreCaseOrderByPriceDesc("VEGETARIAN"), "Món chay đắt nhất là"));
        handlers.put("món chay rẻ nhất", msg -> respond(menuItemRepository.findTopByStatusIgnoreCaseOrderByPriceAsc("VEGETARIAN"), "Món chay rẻ nhất là"));
        handlers.put("món trong ngân sách", this::handleWithinBudget);
        handlers.put("combo ngon nhất", msg -> formatWithGemini("Combo ngon nhất hiện tại gồm *Lẩu Thái Hải Sản* và *Ba Chỉ Bò Mỹ Nướng*. Giá hợp lý và hương vị tuyệt vời! 😋"));

        // handlers với danh mục
        handlers.put("món lẩu đắt nhất", msg -> handleCategoryTopPrice("lẩu", true));
        handlers.put("món nướng thịt rẻ nhất", msg -> handleCategoryTopPrice("nướng thịt", false));
        handlers.put("khai vị đắt nhất", msg -> handleCategoryTopPrice("khai vị", true));
        handlers.put("đồ uống rẻ nhất", msg -> handleCategoryTopPrice("đồ uống", false));
    }

    public String generateReply(String userMessage) {
        String lower = userMessage.toLowerCase();
        for (Map.Entry<String, Function<String, String>> entry : handlers.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue().apply(userMessage); // đã format trong hàm luôn
            }
        }
        return fallbackToGemini(userMessage);
    }

    private String handleCategoryTopPrice(String alias, boolean isDesc) {
        String categoryName = categoryAlias.getOrDefault(alias, alias);
        MenuItem item = isDesc
                ? menuItemRepository.findTopByCategories_NameIgnoreCaseOrderByPriceDesc(categoryName)
                : menuItemRepository.findTopByCategories_NameIgnoreCaseOrderByPriceAsc(categoryName);

        if (item == null) return "Không tìm thấy món thuộc danh mục **" + categoryName + "**.";

        String prefix = isDesc ? "Món " + alias + " đắt nhất là" : "Món " + alias + " rẻ nhất là";
        return formatWithGemini(respond(item, prefix));
    }

    private String respond(MenuItem item, String prefix) {
        if (item == null) return "Không có món nào phù hợp.";
        return "%s *%s* với giá %,.0fđ.".formatted(prefix, item.getName(), item.getPrice());
    }

    private String handleWithinBudget(String message) {
        int budget = extractBudget(message);
        List<MenuItem> items = menuItemRepository.findByPriceLessThanEqual(budget);
        if (items.isEmpty()) return "Không có món nào phù hợp với ngân sách " + budget + "đ.";

        String list = items.stream()
                .map(i -> "- " + i.getName() + " (giá: " + i.getPrice().intValue() + "đ)")
                .collect(Collectors.joining("\n"));

        String reply = "Các món phù hợp với ngân sách " + budget + "đ:\n" + list;
        return formatWithGemini(reply);
    }

    private int extractBudget(String msg) {
        return Arrays.stream(msg.split("\\D+"))
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(Integer.MAX_VALUE);
    }

    private String fallbackToGemini(String message) {
        List<MenuItem> allItems = menuItemRepository.findAll();
        if (allItems.isEmpty()) return "Không có món ăn nào trong hệ thống.";

        String menu = allItems.stream()
                .map(i -> "- " + i.getName() + " (" + i.getPrice().intValue() + "đ)")
                .collect(Collectors.joining("\n"));

        String prompt = """
                %s

                Dựa vào thực đơn sau, hãy tư vấn món ăn phù hợp:
                %s
                """.formatted(message, menu);

        return callGeminiAPI(prompt);
    }

    private String formatWithGemini(String rawText) {
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

            if (!jsonObj.has("candidates")) return "Gemini không trả về kết quả hợp lệ.";
            return jsonObj.getJSONArray("candidates")
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
