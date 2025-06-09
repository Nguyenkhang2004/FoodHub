package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.ScanRequest;
import com.example.FoodHub.dto.response.ScanResponse;
import com.example.FoodHub.entity.QrScanLog;
import com.example.FoodHub.repository.OrderSessionRepository;
import com.example.FoodHub.repository.QrScanLogRepository;
import com.example.FoodHub.repository.RestaurantTableRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import com.example.FoodHub.entity.OrderSession;
import com.example.FoodHub.entity.RestaurantTable;
import com.example.FoodHub.enums.SessionStatus;
import com.example.FoodHub.enums.TableStatus;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScanService {
    RestaurantTableRepository tableRepository;
    OrderSessionRepository sessionRepository;
    QrScanLogRepository qrScanLogRepository;

    @Transactional
    public ScanResponse scanQRCode(ScanRequest request) {
        RestaurantTable table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid table ID"));

        if (!table.getQrCode().equals(request.getQrToken())) {
            throw new IllegalArgumentException("Invalid QR token");
        }

        // Kiểm tra nếu bàn đang bị khóa
        Optional<OrderSession> lockedSession = sessionRepository.findByTableIdAndStatus(request.getTableId(), SessionStatus.LOCKED.name());
        if (lockedSession.isPresent()) {
            throw new IllegalStateException("Bàn đang được sử dụng, vui lòng chờ");
        }

        // Tìm phiên ACTIVE nếu có
        Optional<OrderSession> activeSession = sessionRepository.findByTableIdAndStatus(request.getTableId(), SessionStatus.ACTIVE.name());
        if (activeSession.isPresent() && activeSession.get().getExpiresAt().isAfter(Instant.now())) {
            // Khóa phiên ngay lập tức
            activeSession.get().setStatus(SessionStatus.LOCKED.name());
            sessionRepository.save(activeSession.get());

            QrScanLog log = new QrScanLog();
            log.setTable(table);
            log.setSession(activeSession.get());
            qrScanLogRepository.save(log);

            return new ScanResponse(activeSession.get().getSessionToken(), activeSession.get().getExpiresAt(), "Phiên đã được khóa để đặt món");
        }

        // Nếu không có phiên ACTIVE hoặc bàn trống, tạo phiên mới và khóa ngay
        OrderSession session = new OrderSession();
        session.setTable(table);
        session.setSessionToken(UUID.randomUUID().toString());
        session.setStatus(SessionStatus.LOCKED.name()); // Chuyển thẳng sang LOCKED
        sessionRepository.save(session);

        table.setStatus(TableStatus.OCCUPIED.name());
        tableRepository.save(table);

        QrScanLog log = new QrScanLog();
        log.setTable(table);
        log.setSession(session);
        qrScanLogRepository.save(log);

        return new ScanResponse(session.getSessionToken(), session.getExpiresAt(), "Phiên đã được tạo và khóa để đặt món");
    }

    @Transactional
    public void closeSession(Integer sessionId) {
        OrderSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session ID"));
        RestaurantTable table = session.getTable();

        session.setStatus(SessionStatus.CLOSED.name());
        table.setStatus(TableStatus.AVAILABLE.name());
        sessionRepository.save(session);
        tableRepository.save(table);
    }
}