package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.ScanRequest;
import com.example.FoodHub.dto.response.ScanResponse;
import com.example.FoodHub.entity.QrScanLog;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.repository.OrderSessionRepository;
import com.example.FoodHub.repository.QrScanLogRepository;
import com.example.FoodHub.repository.TableRepository;
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
import com.example.FoodHub.dto.response.ScanResponse;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScanService {
    TableRepository tableRepository;
    OrderSessionRepository sessionRepository;
    QrScanLogRepository qrScanLogRepository;

    @Transactional
    public ScanResponse scanQRCode(ScanRequest request) {
        RestaurantTable table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TABLE_ID));

        if (!table.getQrCode().equals(request.getQrToken())) {
            throw new AppException(ErrorCode.INVALID_TABLE_ID);
        }

        Optional<OrderSession> lockedSession = sessionRepository.findByTableIdAndStatus(request.getTableId(), SessionStatus.LOCKED);
        if (lockedSession.isPresent()) {
            throw new AppException(ErrorCode.OCCUPIED_TABLE);
        }

        if (table.getStatus() == TableStatus.AVAILABLE) {
            OrderSession session = new OrderSession();
            session.setTable(table);
            session.setSessionToken(UUID.randomUUID().toString());
            sessionRepository.save(session);

            table.setStatus(TableStatus.OCCUPIED);
            tableRepository.save(table);

            QrScanLog log = new QrScanLog();
            log.setTable(table);
            log.setSession(session);
            qrScanLogRepository.save(log);

            return new ScanResponse(session.getSessionToken(), session.getExpiresAt(), "Phiên đã được tạo thành công");
        } else {
            Optional<OrderSession> activeSession = sessionRepository.findByTableIdAndStatus(request.getTableId(), SessionStatus.ACTIVE);
            if (activeSession.isPresent() && activeSession.get().getExpiresAt().isAfter(Instant.now())) {
                activeSession.get().setStatus(SessionStatus.LOCKED);
                sessionRepository.save(activeSession.get());

                QrScanLog log = new QrScanLog();
                log.setTable(table);
                log.setSession(activeSession.get());
                qrScanLogRepository.save(log);

                return new ScanResponse(activeSession.get().getSessionToken(), activeSession.get().getExpiresAt(), "Phiên đã được khóa để đặt món");
            } else {
                throw new AppException(ErrorCode.OCCUPIED_TABLE);
            }
        }
    }

    @Transactional
    public void closeSession(Integer sessionId) {
        OrderSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_SESSION_ID));
        RestaurantTable table = session.getTable();

        session.setStatus(SessionStatus.CLOSED);
        table.setStatus(TableStatus.AVAILABLE);
        sessionRepository.save(session);
        tableRepository.save(table);
    }
}