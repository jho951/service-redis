// src/main/java/com/auth/application/JwtHelper.java
package com.auth.application;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class JwtHelper {

    private final byte[] secret;

    public JwtHelper(@Value("${security.jwt.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public ParsedJwt parseAndVerify(String token) {
        try {
            var jwt = SignedJWT.parse(token);
            var ok = jwt.verify(new MACVerifier(secret));
            if (!ok) throw new IllegalArgumentException("Invalid signature");
            var claims = jwt.getJWTClaimsSet();
            return new ParsedJwt(
                    claims.getSubject(),
                    claims.getJWTID(),
                    claims.getExpirationTime().toInstant()
            );
        } catch (ParseException | JOSEException e) {
            throw new IllegalArgumentException("Invalid JWT", e);
        }
    }

    public long secondsLeft(Instant exp, Instant now) {
        long s = ChronoUnit.SECONDS.between(now, exp);
        return Math.max(s, 0);
    }

    public record ParsedJwt(String subject, String jti, Instant expiresAt) {}
}
