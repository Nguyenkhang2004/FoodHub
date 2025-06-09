package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.ScanRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.ScanResponse;
import com.example.FoodHub.service.ScanService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scan")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScanController {
    ScanService scanService;

    @PostMapping
    public ResponseEntity<ApiResponse<ScanResponse>> scanQRCode(@RequestBody ScanRequest request) {
        ScanResponse result = scanService.scanQRCode(request);
        ApiResponse<ScanResponse> response = ApiResponse.<ScanResponse>builder()
                .result(result)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{sessionId}/close")
    public ResponseEntity<Void> closeSession(@PathVariable Integer sessionId) {
        try {
            scanService.closeSession(sessionId);
            return ResponseEntity.ok().build(); // Trả về 200 OK khi thành công
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // Trả về 400 Bad Request nếu lỗi
        }
    }
}
