package com.example.FoodHub.utils;

import com.example.FoodHub.dto.request.IntrospectRequest;
import com.example.FoodHub.dto.response.IntrospectResponse;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${jwt.table-token-duration}")
    private long TABLE_TOKEN_DURATION;

    public String generateTableToken(String tableNumber) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(tableNumber)
                    .issuer("FoodHub")
                    .expirationTime(Date.from(Instant.now().plusSeconds(TABLE_TOKEN_DURATION)))
                    .issueTime(new Date())
                    .jwtID(UUID.randomUUID().toString())
                    .claim("scope", tableNumber + " CREATE_ORDER ADD_NEW_ITEMS VIEW_ORDER PROCESS_PAYMENT")
                    .claim("type", "TABLE_TOKEN")
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS512),
                    claims
            );

            signedJWT.sign(new MACSigner(SIGNER_KEY.getBytes(StandardCharsets.UTF_8)));
            return signedJWT.serialize();
        } catch (Exception e) {
            log.error("Failed to create token for table {}: {}", tableNumber, e.getMessage());
            throw new AppException(ErrorCode.CREATE_TABLE_TOKEN_FAILED);
        }
    }

    public static String getTypeFromToken(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getStringClaim("type");
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_QR_TOKEN);
        }
    }

    public String getTableNumberFromToken(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getStringClaim("scope");
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_QR_TOKEN);
        }
    }

    public String getJwtIdFromToken(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getJWTID();
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_QR_TOKEN);
        }
    }

    public Instant getExpirationDateFromToken(String token) {
        try {
            Date expiry = SignedJWT.parse(token).getJWTClaimsSet().getExpirationTime();
            return expiry.toInstant();
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_QR_TOKEN);
        }
    }

    public boolean isTokenValid(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            boolean isSignatureValid = jwt.verify(new MACVerifier(SIGNER_KEY.getBytes(StandardCharsets.UTF_8)));
            boolean isNotExpired = jwt.getJWTClaimsSet().getExpirationTime().after(new Date());
            return isSignatureValid && isNotExpired;
        } catch (ParseException e) {
            log.error("Định dạng token không hợp lệ: {}", e.getMessage());
            return false;
        } catch (JOSEException e) {
            log.error("Xác thực token thất bại: {}", e.getMessage());
            return false;
        }
    }
}

