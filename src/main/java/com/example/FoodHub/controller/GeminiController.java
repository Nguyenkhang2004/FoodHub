package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.ChatRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.ChatResponse;
import com.example.FoodHub.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = "*") // Cho phép CORS nếu cần (tùy môi trường)
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;

    /**
     * Endpoint để nhận tin nhắn từ người dùng và trả về phản hồi từ Gemini
     * @param request Đối tượng ChatRequest chứa tin nhắn từ người dùng
     * @return ResponseEntity chứa ApiResponse với phản hồi từ Gemini
     */
    @PostMapping("/reply")
    public ResponseEntity<ApiResponse<ChatResponse>> generateReply(@RequestBody ChatRequest request) {
        try {
            if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<ChatResponse>builder()
                                .code(400)
                                .message("Tin nhắn không được để trống!")
                                .build());
            }

            String reply = geminiService.generateReply(request.getMessage());
            ChatResponse chatResponse = new ChatResponse(reply);

            return ResponseEntity.ok()
                    .body(ApiResponse.<ChatResponse>builder()
                            .code(200)
                            .message("Thành công")
                            .result(chatResponse)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.<ChatResponse>builder()
                            .code(500)
                            .message("Đã xảy ra lỗi khi xử lý yêu cầu: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Endpoint để kiểm tra trạng thái dịch vụ (optional)
     * @return ResponseEntity chứa ApiResponse với thông báo trạng thái
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<String>> checkStatus() {
        return ResponseEntity.ok()
                .body(ApiResponse.<String>builder()
                        .code(200)
                        .message("Gemini Service is running successfully!")
                        .build());
    }
}