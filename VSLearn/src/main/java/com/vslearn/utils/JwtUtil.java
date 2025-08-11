package com.vslearn.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.vslearn.constant.ConstantVariables;

import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
public class JwtUtil {
    public String generateToken(String id, String email, String role) {
        JWSHeader jwtHeader = new JWSHeader(JWSAlgorithm.HS256);
        Date now = new Date();
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(email)
                .issuer("VSLearn")
                .issueTime(now)
                .expirationTime(new Date(now.getTime() + ConstantVariables.KEY_TIME_OUT))
                .claim("id", id)
                .claim("scope", role)
                .build();
        Payload jwtPayload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwtHeader, jwtPayload);
        try {
            jwsObject.sign(new MACSigner(ConstantVariables.SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public JWTClaimsSet getClaimsFromToken(String token) {
       try {
           SignedJWT signedJWT = SignedJWT.parse(token);
           return signedJWT.getJWTClaimsSet();
       } catch (ParseException e) {
           throw new RuntimeException(e);
       }
    }
}
