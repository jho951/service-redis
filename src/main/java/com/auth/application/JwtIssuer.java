package com.auth.application;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtIssuer {

    private final byte[] secret;
    private final JWSSigner signer;

    public JwtIssuer(@Value("${security.jwt.secret}") String secret) throws KeyLengthException {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.signer = new MACSigner(new SecretKeySpec(this.secret, "HmacSHA256"));
    }

    public String issueAccessToken(String subjectUserId,
                                   List<String> roles,
                                   String jti,
                                   Instant now,
                                   long ttlSeconds) {
        return sign(subjectUserId, roles, jti, now, ttlSeconds);
    }

    public String issueRefreshToken(String subjectUserId,
                                    String jti,
                                    Instant now,
                                    long ttlSeconds) {
        // roles 없이 간단하게
        return sign(subjectUserId, null, jti, now, ttlSeconds);
    }

    private String sign(String sub, List<String> roles, String jti, Instant now, long ttlSec) {
        try {
            var claims = new JWTClaimsSet.Builder()
                    .subject(sub)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(ttlSec)))
                    .jwtID(jti);
            if (roles != null) claims.claim("roles", roles);

            var signed = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(),
                    claims.build()
            );
            signed.sign(signer);
            return signed.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("JWT signing failed", e);
        }
    }
}
