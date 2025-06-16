package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.ScanRequest;
import com.example.FoodHub.dto.response.AuthenticationResponse;
import com.example.FoodHub.dto.response.ScanResponse;
import com.example.FoodHub.entity.QrScanLog;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.repository.OrderSessionRepository;
import com.example.FoodHub.repository.QrScanLogRepository;
import com.example.FoodHub.repository.RestaurantTableRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import com.example.FoodHub.entity.OrderSession;
import com.example.FoodHub.entity.RestaurantTable;
import com.example.FoodHub.enums.SessionStatus;
import com.example.FoodHub.enums.TableStatus;
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScanService {
    RestaurantTableRepository tableRepository;
    OrderSessionRepository sessionRepository;
    QrScanLogRepository qrScanLogRepository;
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    private String generateScanQRToken(String tableNumber) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(tableNumber)
                .issuer("FoodHub")
                .expirationTime(Date.from(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS)))
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", "SCAN_QR")
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Error signing JWT for QR code: {}", e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public ScanResponse authenticateForScanQR(ScanRequest request){
        var table = tableRepository.findByTableNumber(request.getTableNumber())
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
        if (table.getStatus() == null || !table.getStatus().equals("AVAILABLE")) {
            throw new AppException(ErrorCode.TABLE_NOT_AVAILABLE);
        }
        String token = generateScanQRToken(table.getTableNumber());
        return ScanResponse.builder()
                .scanQRToken(token)
                .build();
    }



}