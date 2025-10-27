package com.config.security;

import com.auth.infrastructure.token.TokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisBlacklistValidator implements OAuth2TokenValidator<Jwt> {
    private final TokenStore tokenStore;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String jti = token.getId(); // JWT의 jti
        if (jti != null && tokenStore.isAccessTokenBlacklisted(jti)) {
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Token has been revoked", null)
            );
        }
        return OAuth2TokenValidatorResult.success();
    }
}
