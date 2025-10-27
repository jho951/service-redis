package com.auth.infrastructure.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TokenStore {
    private final StringRedisTemplate redis;

    /* Access 토큰 블랙리스트: bl:at:{jti} */
    public void blacklistAccessToken(String jti, long secondsToExpire) {
        String key = "bl:at:" + jti;
        redis.opsForValue().set(key, "1", Duration.ofSeconds(secondsToExpire));
    }

    public boolean isAccessTokenBlacklisted(String jti) {
        return Boolean.TRUE.equals(redis.hasKey("bl:at:" + jti));
    }

    /* Refresh 토큰 저장: rt:{userId}:{jti} */
    public void saveRefreshToken(String userId, String jti, String tokenValue, long ttlSeconds) {
        String key = "rt:" + userId + ":" + jti;
        redis.opsForValue().set(key, tokenValue, ttlSeconds, TimeUnit.SECONDS);
    }

    public String getRefreshToken(String userId, String jti) {
        return redis.opsForValue().get("rt:" + userId + ":" + jti);
    }

    public void deleteRefreshToken(String userId, String jti) {
        redis.delete("rt:" + userId + ":" + jti);
    }
}
