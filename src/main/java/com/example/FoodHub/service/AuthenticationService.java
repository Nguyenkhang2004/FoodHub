package com.example.FoodHub.service;


import com.example.FoodHub.dto.request.AuthenticationRequest;
import com.example.FoodHub.dto.request.IntrospectRequest;
import com.example.FoodHub.dto.request.LogoutRequest;
import com.example.FoodHub.dto.request.RefreshRequest;
import com.example.FoodHub.dto.response.AuthenticationResponse;
import com.example.FoodHub.dto.response.IntrospectResponse;
import com.example.FoodHub.entity.InvalidateToken;
import com.example.FoodHub.entity.Role;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.repository.InvalidateTokenRepository;
import com.example.FoodHub.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidateTokenRepository invalidateTokenRepository;
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;
    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;
    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public String generateToken(User user){
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("FoodHub")
                .expirationTime(Date.from(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS)))
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toString());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        try{
            jwsObject.sign(new MACSigner(SIGNER_KEY));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Error signing JWT: {}", e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .isAuthenticated(authenticated)
                .build();
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException {
        String token = request.getToken();
        boolean isValid = true;
        try{
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }
        return IntrospectResponse.builder()
                .isValid(isValid)
                .build();
    }

    public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY);
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean verified = signedJWT.verify(verifier);
        if(!(verified && expiryTime.after(new Date()))
                && invalidateTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        return signedJWT;
    }

        public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
            SignedJWT signedJWT = verifyToken(request.getToken(), true);
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            InvalidateToken invalidateToken = InvalidateToken.builder()
                    .token(jti)
                    .expiryTime(expiryTime.toInstant())
                    .build();
            invalidateTokenRepository.save(invalidateToken);
            User user = userRepository.findByEmail(signedJWT.getJWTClaimsSet().getSubject()).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
            String token = generateToken(user);
            return AuthenticationResponse.builder()
                    .token(token)
                    .isAuthenticated(true)
                    .build();
        }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            SignedJWT signedJWT = verifyToken(request.getToken(), true);
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            InvalidateToken invalidateToken = InvalidateToken.builder()
                    .token(jti)
                    .expiryTime(expiryTime.toInstant())
                    .build();
            invalidateTokenRepository.save(invalidateToken);
        } catch (AppException e){
            log.info("Token already expired", e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        Role role = user.getRoleName();
        if(role != null) {
            stringJoiner.add("ROLE_" + role.getName());
            role.getPermissions().forEach(permission -> {
                stringJoiner.add(permission.getName());
            });
        }
        return stringJoiner.toString();
    }
}
