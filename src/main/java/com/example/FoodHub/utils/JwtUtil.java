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

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.table-token-expiration}") // 2 hours in seconds
    protected long TABLE_TOKEN_DURATION;

    public String generateTableToken(String tableNumber) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(tableNumber)
                .issuer("FoodHub")
                .expirationTime(Date.from(Instant.now().plus(TABLE_TOKEN_DURATION, ChronoUnit.SECONDS)))
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .claim("tableNumber", tableNumber)
                .claim("type", "TABLE_TOKEN")
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Error signing table JWT: {}", e.getMessage());
            throw new AppException(ErrorCode.CREATE_TABLE_TOKEN_FAILED);
        }
    }



    public String getTableNumberFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getStringClaim("tableNumber");
        } catch (ParseException e) {
            log.error("Error parsing table number from token: {}", e.getMessage());
            throw new AppException(ErrorCode.INVALID_QR_TOKEN);
        }
    }

    public String getJwtIdFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getJWTID();
        } catch (ParseException e) {
            log.error("Error parsing JWT ID from token: {}", e.getMessage());
            throw new AppException(ErrorCode.INVALID_QR_TOKEN);
        }
    }

    public Instant getExpirationDateFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expiration.toInstant();

        } catch (ParseException e) {
            log.error("Error parsing expiration from token: {}", e.getMessage());
            throw new AppException(ErrorCode.INVALID_QR_TOKEN);
        }
    }

    public boolean isTokenValid(String token) {
        try {
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY);
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Verify signature
            boolean verified = signedJWT.verify(verifier);
            if (!verified) {
                return false;
            }

            // Check expiration
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expirationTime.after(new Date());

        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }

    public SignedJWT parseAndValidateToken(String token) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY);
        SignedJWT signedJWT = SignedJWT.parse(token);

        boolean verified = signedJWT.verify(verifier);
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        if (!verified || !expirationTime.after(new Date())) {
            throw new AppException(ErrorCode.INVALID_QR_TOKEN);
        }

        return signedJWT;
    }
}
