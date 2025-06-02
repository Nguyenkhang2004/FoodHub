package com.example.FoodHub.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GeminiService {

    private static final String API_KEY = "AIzaSyCHLZsfZyROL8zEN6Mc9yVkPqwePEUCLnc";
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    public String generateReply(String userMessage) {
        try {
            HttpClient client = HttpClient.newHttpClient();
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
            """.formatted(userMessage);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // ✅ In toàn bộ nội dung phản hồi từ Gemini để debug
            System.out.println("RESPONSE FROM GEMINI:\n" + response.body());

            JSONObject jsonObj = new JSONObject(response.body());

            // ⚠️ Kiểm tra nếu không có "candidates"
            if (!jsonObj.has("candidates")) {
                return "Gemini không trả về kết quả hợp lệ.";
            }

            String reply = jsonObj
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            return reply;

        } catch (Exception e) {
            System.out.println("LỖI GỌI GEMINI API:");
            e.printStackTrace();
            return "Đã có lỗi xảy ra khi gọi Gemini API.";
        }
    }
}
