package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.LogoutRequest;
import com.example.FoodHub.dto.request.RefreshRequest;
import com.example.FoodHub.dto.request.ScanQRRequest;
import com.example.FoodHub.dto.request.TokenValidationRequest;
import com.example.FoodHub.dto.response.*;
import com.example.FoodHub.entity.InvalidateToken;
import com.example.FoodHub.service.ScanQRService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qr")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScanQRController {

    ScanQRService scanQRService;

    /**
     * API quét QR code
     */
    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<ScanQRResponse>> scanQR(@RequestBody ScanQRRequest request) {
        log.info("Scanning QR code: {}", request.getQrCode());

            ScanQRResponse scanQRResponse = scanQRService.scanQRCode(request);

            return ResponseEntity.ok(ApiResponse.<ScanQRResponse>builder()
                    .message("Quét QR code thành công")
                    .result(scanQRResponse)
                    .build());

    }

    /**
     * API kiểm tra token
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(@RequestBody @Valid TokenValidationRequest request) {
        log.debug("Validating token for table access");

        TokenValidationResponse result = scanQRService.isValidToken(request.getToken());

        return ResponseEntity.ok(ApiResponse.<TokenValidationResponse>builder()
                .message("Xác thực token thành công")
                .result(result)
                .build());
    }



    /**
     * API kết thúc phiên (khách thanh toán xong)
     */
    @PostMapping("/finish-session")
    public ResponseEntity<ApiResponse<Void>> finishSession(@RequestBody LogoutRequest request) {
        log.info("Kết thúc phiên với token: {}", request.getToken());

        scanQRService.finishSession(request.getToken());

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Kết thúc phiên thành công")
                .build());
    }

    /**
     * Gia hạn token (refresh)
     * POST /api/qr/refresh-token
     */
//    @PostMapping("/refresh-token")
//    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@RequestBody RefreshRequest request) {
//        TokenRefreshResponse refreshResponse = scanQRService.refreshToken(request.getToken());
//
//        return ResponseEntity.ok(ApiResponse.<TokenRefreshResponse>builder()
//                .message("Gia hạn token thành công")
//                .result(refreshResponse)
//                .build());
//    }
}
