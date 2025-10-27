package com.auth.application;

import com.auth.infrastructure.token.TokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService {
    private final TokenStore tokenStore;

    public void logout(String userId, String accessJti, long accessSecondsLeft, String refreshJti) {
        if (accessJti != null && accessSecondsLeft > 0) {
            tokenStore.blacklistAccessToken(accessJti, accessSecondsLeft);
        }
        if (refreshJti != null) {
            tokenStore.deleteRefreshToken(userId, refreshJti);
        }
    }
}
