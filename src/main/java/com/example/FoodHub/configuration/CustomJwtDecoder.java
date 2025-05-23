package com.example.FoodHub.configuration;


import com.example.FoodHub.dto.request.IntrospectRequest;
import com.example.FoodHub.dto.response.IntrospectResponse;
import com.example.FoodHub.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;
    @Autowired
    private AuthenticationService authenticationService;
    private NimbusJwtDecoder nimbusJwtDecoder = null;
    @Override
    public Jwt decode(String token) throws JwtException {
        try{
            IntrospectResponse response = authenticationService.introspect(IntrospectRequest.builder().token(token).build());
            if(!response.isValid()) throw new JwtException("Token is invalid");
        } catch (JOSEException | ParseException e){
            throw new JwtException("Invalid token: " + e.getMessage());
        }
        if(nimbusJwtDecoder == null){
            SecretKeySpec key = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS512).build();
        }
        return nimbusJwtDecoder.decode(token);
    }
}
