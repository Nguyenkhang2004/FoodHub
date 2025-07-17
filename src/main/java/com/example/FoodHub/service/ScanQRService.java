package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.ScanQRRequest;
import com.example.FoodHub.dto.response.ScanQRResponse;
import com.example.FoodHub.dto.response.TokenRefreshResponse;
import com.example.FoodHub.dto.response.TokenValidationResponse;
import com.example.FoodHub.entity.*;
import com.example.FoodHub.enums.OrderStatus;
import com.example.FoodHub.enums.TableStatus;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.repository.InvalidateTokenRepository;
import com.example.FoodHub.repository.RestaurantOrderRepository;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScanQRService {

    RestaurantTableRepository tableRepository;
    InvalidateTokenRepository invalidTokenRepository;
    RestaurantOrderRepository orderRepository;
    JwtUtil jwtUtil;

    private static final int TOKEN_EXPIRY_HOURS = 2;

    /**
     * Quét QR để bắt đầu phiên: Tạo token nếu bàn AVAILABLE hoặc token đã hết hạn.
     */
    @Transactional
    public ScanQRResponse scanQRCode(ScanQRRequest request) {
        RestaurantTable table = tableRepository.findByQrCodeWithLock(request.getQrCode())
                .orElseThrow(() -> new AppException(ErrorCode.QR_CODE_INVALID));

        Instant now = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        boolean tokenExpired = table.getTokenExpiry() == null ||
                (table.getCurrentToken() != null && !jwtUtil.isTokenValid(table.getCurrentToken())) ||
                table.getTokenExpiry().isBefore(now);

//        Integer activeOrderId = orderRepository.findActiveOrderIdByTable(table.getId());
//
//        boolean isCompleted = activeOrderId != null &&
//                orderRepository.findById(activeOrderId)
//                        .map(order -> order.getStatus().equals(OrderStatus.COMPLETED.name()))
//                        .orElse(false);

        if (table.getStatus().equals(TableStatus.AVAILABLE.name()) || tokenExpired) {
            if (table.getCurrentToken() != null) {
                invalidateToken(table.getCurrentToken());
            }

            String newToken = jwtUtil.generateTableToken(table.getTableNumber());
            Instant expiry = now.plus(TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS);

            table.setCurrentToken(newToken);
            table.setTokenExpiry(expiry);
//            table.setStatus(TableStatus.OCCUPIED.name());
            tableRepository.save(table);

            return ScanQRResponse.builder()
                    .token(newToken)
                    .tableNumber(table.getTableNumber())
                    .expiryTime(expiry)
                    .orderId(null) // chưa có đơn
                    .build();
        }

        if (!jwtUtil.isTokenValid(table.getCurrentToken()) || isInvalidated(table.getCurrentToken())) {
            throw new AppException(ErrorCode.INVALID_QR_TOKEN);
        }

        return ScanQRResponse.builder()
                .token(table.getCurrentToken())
                .tableNumber(table.getTableNumber())
                .expiryTime(table.getTokenExpiry())
                .orderId(orderRepository.findActiveOrderIdByTable(table.getId()))
                .build();
    }

    public TokenValidationResponse isValidToken(String token) {
        try {
            if (!jwtUtil.isTokenValid(token)) {
                invalidateToken(token);
                return TokenValidationResponse.builder().isValid(false).build();
            }
            boolean isInvalid = invalidTokenRepository.existsByToken(jwtUtil.getJwtIdFromToken(token));
            return TokenValidationResponse.builder().isValid(!isInvalid).build();
        } catch (Exception e) {
            return TokenValidationResponse.builder().isValid(false).build();
        }
    }

    @Transactional
    public void finishSession(String token) {
        String tableNumber = jwtUtil.getTableNumberFromToken(token);
        log.info("Finishing session for table: {}, token: {}", tableNumber, token);
        RestaurantTable table = tableRepository.findByTableNumberWithLock(tableNumber)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));

        if (!token.equals(table.getCurrentToken())) {
            throw new AppException(ErrorCode.INVALID_QR_TOKEN);
        }

        invalidateToken(token);
        table.setCurrentToken(null);
        table.setTokenExpiry(null);
        table.setStatus(TableStatus.AVAILABLE.name());
        tableRepository.save(table);
    }

    @Scheduled(fixedRate = 300_000) // every 5 minutes
    public void cleanupExpiredTokens() {
        Instant now = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        List<RestaurantTable> expiredTables = tableRepository.findByTokenExpiryBefore(now);
        for (RestaurantTable table : expiredTables) {
            boolean hasActiveOrder = orderRepository.hasActiveOrder(table.getId());
            if (!hasActiveOrder) {
                if (table.getCurrentToken() != null) {
                    invalidateToken(table.getCurrentToken());
                }
                table.setCurrentToken(null);
                table.setTokenExpiry(null);
                table.setStatus(TableStatus.AVAILABLE.name());
            }
        }
        tableRepository.saveAll(expiredTables);
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupInvalidTokens() {
        Instant now = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        int deleted = invalidTokenRepository.deleteExpiredTokens(now);
        log.info("Deleted {} expired tokens", deleted);
    }

    private void invalidateToken(String token) {
        String jwtId = jwtUtil.getJwtIdFromToken(token);
        InvalidateToken invalidToken = InvalidateToken.builder()
                .token(jwtId)
                .expiryTime(jwtUtil.getExpirationDateFromToken(token))
                .build();
        invalidTokenRepository.save(invalidToken);
    }

    private boolean isInvalidated(String token) {
        String jwtId = jwtUtil.getJwtIdFromToken(token);
        return invalidTokenRepository.existsByToken(jwtId);
    }
}
