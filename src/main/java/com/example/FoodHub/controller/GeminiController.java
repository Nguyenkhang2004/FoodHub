package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.ChatRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.ChatResponse;
import com.example.FoodHub.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = "http://127.0.0.1:5500") // Có thể cấu hình động theo profile sau này
@RequiredArgsConstructor
@Slf4j
public class GeminiController {

    private final GeminiService geminiService;

    /**
     * Nhận message từ người dùng và trả về phản hồi từ Gemini.
     */
    @PostMapping("/reply")
    public ResponseEntity<ApiResponse<ChatResponse>> generateReply(@RequestBody ChatRequest request) {
        if (isBlank(request) || isBlank(request.getMessage())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<ChatResponse>builder()
                            .code(400)
                            .message("Tin nhắn không được để trống!")
                            .build()
            );
        }

        try {
            String reply = geminiService.generateReply(request.getMessage());
            ChatResponse chatResponse = new ChatResponse(reply);

            return ResponseEntity.ok(
                    ApiResponse.<ChatResponse>builder()
                            .code(200)
                            .message("Thành công")
                            .result(chatResponse)
                            .build()
            );
        } catch (Exception e) {
            log.error("Lỗi khi xử lý Gemini", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.<ChatResponse>builder()
                            .code(500)
                            .message("Đã xảy ra lỗi khi xử lý yêu cầu: " + e.getMessage())
                            .build()
            );
        }
    }

    /**
     * Kiểm tra trạng thái dịch vụ Gemini.
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<String>> checkStatus() {
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .code(200)
                        .message("Gemini Service is running successfully!")
                        .build()
        );
    }

    /**
     * Kiểm tra null hoặc rỗng.
     */
    private boolean isBlank(Object obj) {
        return obj == null || obj.toString().trim().isEmpty();
    }
}
