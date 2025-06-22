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
public class    ScanQRController {

    ScanQRService scanQRService;

    /**
     * API quét QR code
     */
    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<ScanQRResponse>> scanQR(@RequestBody ScanQRRequest request) {
        log.info("Scanning QR code: {}", request.getQrCode());
        try {
            ScanQRResponse scanQRResponse = scanQRService.scanQRCode(request);
            ApiResponse<ScanQRResponse> response = ApiResponse.<ScanQRResponse>builder()
                    .message("Quét QR code thành công")
                    .result(scanQRResponse)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error scanning QR code: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * API kiểm tra token
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(@RequestBody @Valid TokenValidationRequest request) {
        log.debug("Validating token for table access");

        TokenValidationResponse result = scanQRService.isValidToken(request.getToken());

        ApiResponse<TokenValidationResponse> response = ApiResponse.<TokenValidationResponse>builder()
                .message("Xác thực token thành công")
                .result(result)
                .build();
        return ResponseEntity.ok(response);
    }

//    /**
//     * API lấy thông tin bàn
//     */
//    @GetMapping("/table-info")
//    public ResponseEntity<ApiResponse<TableInfo>> getTableInfo(@RequestHeader("Authorization") String authHeader) {
//        try {
//            String token = authHeader.replace("Bearer ", "");
//            TableInfo tableInfo = scanQRService.getTableInfo(token);
//            return ResponseEntity.ok(ApiResponse.success(tableInfo));
//        } catch (InvalidTokenException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(ApiResponse.error("INVALID_TOKEN", e.getMessage()));
//        } catch (Exception e) {
//            log.error("Error getting table info: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(ApiResponse.error("INTERNAL_ERROR", "Có lỗi xảy ra"));
//        }
//    }

    /**
     * API kết thúc phiên (khách thanh toán xong)
     */
    @PostMapping("/finish-session")
    public ResponseEntity<ApiResponse<Void>> finishSession(@RequestBody LogoutRequest request) {

        scanQRService.finishSession(request.getToken());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Kết thúc phiên thành công")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Gia hạn token (refresh)
     * POST /api/qr/refresh-token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(RefreshRequest request) {


        TokenRefreshResponse refreshResponse = scanQRService.refreshTableToken(request.getToken());
        ApiResponse<TokenRefreshResponse> response = ApiResponse.<TokenRefreshResponse>builder()
                .message("Gia hạn token thành công")
                .result(refreshResponse)
                .build();

        return ResponseEntity.ok(response);
    }
}
