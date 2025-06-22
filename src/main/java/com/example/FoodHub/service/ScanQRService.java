package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.ScanQRRequest;
import com.example.FoodHub.dto.request.TokenValidationRequest;
import com.example.FoodHub.dto.response.ScanQRResponse;
import com.example.FoodHub.dto.response.TokenRefreshResponse;
import com.example.FoodHub.dto.response.TokenValidationResponse;
import com.example.FoodHub.entity.*;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.repository.InvalidateTokenRepository;
import com.example.FoodHub.repository.RestaurantTableRepository;
import com.example.FoodHub.utils.JwtUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import com.example.FoodHub.enums.TableStatus;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScanQRService {

    RestaurantTableRepository tableRepository;
    InvalidateTokenRepository invalidTokenRepository;
    JwtUtil jwtUtil;

    private static final int TOKEN_EXPIRY_HOURS = 2;

    /**
     * Quét QR code để lấy token
     */
    public ScanQRResponse scanQRCode(ScanQRRequest request){
        // Tìm bàn theo QR code
        log.info("Processing QR scan request for code: {}", request.getQrCode());
        RestaurantTable table = tableRepository.findByQrCode(request.getQrCode())
                .orElseThrow(() -> new AppException(ErrorCode.QR_CODE_INVALID));

        // Kiểm tra bàn đã có người ngồi chưa
        if (table.getStatus().equals(TableStatus.OCCUPIED.name()) &&
                table.getCurrentToken() != null &&
                table.getTokenExpiry() != null &&
                table.getTokenExpiry().isAfter(Instant.now())) {

            // Kiểm tra token có bị invalidate chưa (sử dụng JWT ID)
            String currentJwtId = jwtUtil.getJwtIdFromToken(table.getCurrentToken());
            if (!invalidTokenRepository.existsByToken(currentJwtId)) {
                throw new AppException(ErrorCode.OCCUPIED_TABLE);
            }
        }

        // Tạo token mới
        String newToken = jwtUtil.generateTableToken(table.getTableNumber());
        Instant expiry = Instant.now().plus(TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS);

        // Cập nhật thông tin bàn
        table.setCurrentToken(newToken);
        table.setTokenExpiry(expiry);
//        table.setStatus(TableStatus.OCCUPIED.name());
        tableRepository.save(table);

        log.info("Generated new table token for table: {} with QR: {}", table.getTableNumber(), request.getQrCode());

        return ScanQRResponse.builder()
                .token(newToken)
                .tableNumber(table.getTableNumber())
                .expiryTime(expiry)
                .build();
    }

    /**
     * Kiểm tra token có hợp lệ không
     */
    public TokenValidationResponse isValidToken(String token) {
        boolean isValid = true;

        try {
            // Kiểm tra JWT ID có bị invalidate chưa
            String jwtId = jwtUtil.getJwtIdFromToken(token);
            if (invalidTokenRepository.existsByToken(jwtId)) {
                isValid = false;
            }

            // Kiểm tra token có hợp lệ và chưa hết hạn
            if (!jwtUtil.isTokenValid(token)) {
                isValid = false;
            }

            // Kiểm tra token có khớp với bàn hiện tại không
            String tableNumber = jwtUtil.getTableNumberFromToken(token);
            RestaurantTable table = tableRepository.findByTableNumber(tableNumber)
                    .orElse(null);

            if (table == null || !token.equals(table.getCurrentToken())) {
                isValid = false;
            }
            if (table.getTokenExpiry() == null || table.getTokenExpiry().isBefore(Instant.now())) {
                isValid = false;
            }

        } catch (Exception e) {
            log.error("Error validating table token: {}", e.getMessage());
            isValid = false;
        }
        return TokenValidationResponse.builder()
                .valid(isValid)
                .build();
    }

    /**
     * Kết thúc phiên và invalidate token
     */
    public void finishSession(String token) {
        try {
            String tableNumber = jwtUtil.getTableNumberFromToken(token);
            String jwtId = jwtUtil.getJwtIdFromToken(token);

            RestaurantTable table = tableRepository.findByTableNumber(tableNumber)
                    .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));

            // Thêm JWT ID vào danh sách invalid
            InvalidateToken invalidToken = InvalidateToken.builder()
                    .token(jwtId)
                    .expiryTime(jwtUtil.getExpirationDateFromToken(token))
//                    .tokenType("TABLE_TOKEN")
                    .build();
            invalidTokenRepository.save(invalidToken);

            // Cập nhật trạng thái bàn
            table.setStatus(TableStatus.AVAILABLE.name());
            table.setCurrentToken(null);
            table.setTokenExpiry(null);
            tableRepository.save(table);

            log.info("Table session finished for table: {}", tableNumber);

        } catch (Exception e) {
            log.error("Error finishing table session: {}", e.getMessage());
            throw new AppException(ErrorCode.SESSION_FINISH_EXCEPTION);
        }
    }

    /**
     * Lấy thông tin bàn từ token
     */
//    public TableInfo getTableInfo(String token) {
//        if (!isValidToken(token)) {
//            throw new AppException(ErrorCode.INVALID_QR_TOKEN);
//        }
//
//        String tableNumber = jwtUtil.getTableNumberFromToken(token);
//        RestaurantTable table = tableRepository.findByTableNumber(tableNumber)
//                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
//
//        return TableInfo.builder()
//                .tableNumber(table.getTableNumber())
//                .area(table.getArea())
//                .status(table.getStatus())
//                .tokenExpiry(table.getTokenExpiry())
//                .build();
//    }

    public TokenRefreshResponse refreshTableToken(String currentToken) {
        try {
            // Kiểm tra token hiện tại có hợp lệ không
            if (!isValidToken(currentToken).isValid()) {
                throw new AppException(ErrorCode.INVALID_QR_TOKEN);
            }

            String tableNumber = jwtUtil.getTableNumberFromToken(currentToken);
            String currentJwtId = jwtUtil.getJwtIdFromToken(currentToken);

            RestaurantTable table = tableRepository.findByTableNumber(tableNumber)
                    .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));

            // Invalidate token cũ
            InvalidateToken invalidToken = InvalidateToken.builder()
                    .token(currentJwtId)
                    .expiryTime(jwtUtil.getExpirationDateFromToken(currentToken))
//                    .createdAt(Instant.now())
//                    .tokenType("TABLE_TOKEN")
                    .build();
            invalidTokenRepository.save(invalidToken);

            // Tạo token mới
            String newToken = jwtUtil.generateTableToken(tableNumber);
            Instant newExpiry = Instant.now().plus(TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS);

            // Cập nhật bàn
            table.setCurrentToken(newToken);
            table.setTokenExpiry(newExpiry);
            tableRepository.save(table);

            log.info("Refreshed token for table: {}", tableNumber);
            return TokenRefreshResponse.builder()
                    .newToken(newToken)
                    .expiryTime(newExpiry)
                    .build();


        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new RuntimeException("Không thể gia hạn token", e);
        }
    }

    /**
     * Cleanup expired tokens (chạy theo schedule)
     */
    @Scheduled(fixedRate = 3600000) // Chạy mỗi giờ
    public void cleanupExpiredTokens() {
        try {
            Instant now = Instant.now();

            // Xóa expired tokens từ invalid_tokens table
            invalidTokenRepository.deleteExpiredTokens(now);

            // Cập nhật các bàn có token hết hạn
            List<RestaurantTable> expiredTables = tableRepository.findAll().stream()
                    .filter(table -> table.getTokenExpiry() != null &&
                            table.getTokenExpiry().isBefore(now.atZone(ZoneId.systemDefault()).toInstant()))
                    .collect(Collectors.toList());

            for (RestaurantTable table : expiredTables) {
                table.setStatus(TableStatus.AVAILABLE.name());
                table.setCurrentToken(null);
                table.setTokenExpiry(null);
            }

            if (!expiredTables.isEmpty()) {
                tableRepository.saveAll(expiredTables);
                log.info("Cleaned up {} expired table sessions", expiredTables.size());
            }

        } catch (Exception e) {
            log.error("Error during table token cleanup: {}", e.getMessage());
        }
    }
}