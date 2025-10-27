package com.auth.application;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class JwtTokenService {

    private final JwtEncoder encoder;
    private final long accessTokenTtlSeconds;

    public JwtTokenService(
            @Value("${security.jwt.secret:local-dev-secret-local-dev-secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds:3600}") long accessTokenTtlSeconds
    ) {
        SecretKey key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    /** 기존 메서드 (원본) */
    public String generate(Authentication authentication) {
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenTtlSeconds);

        List<String> scopes = authorities.stream().map(GrantedAuthority::getAuthority).toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("drawer-api")
                .issuedAt(now)
                .expiresAt(exp)
                .subject(username)
                .claim("scope", String.join(" ", scopes))
                .claim("roles", scopes)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(() -> "HS256").type("JWT").build();
        return this.encoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    /* ====== 별칭/오버로드: 호출부가 create(...)를 기대할 때 대비 ====== */

    /** Authentication 기반 별칭 */
    public String create(Authentication authentication) {
        return generate(authentication);
    }

    /** UserDetails 기반 별칭 */
    public String create(UserDetails user) {
        return create(user.getUsername(),
                user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }

    /** username + roles(권한 문자열) 기반 별칭 */
    public String create(String username, Collection<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenTtlSeconds);

        List<String> scopes = roles.stream().toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("drawer-api")
                .issuedAt(now)
                .expiresAt(exp)
                .subject(username)
                .claim("scope", String.join(" ", scopes))
                .claim("roles", scopes)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(() -> "HS256").type("JWT").build();
        return this.encoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }
}
