
package com.auth.interfaces.dto;

import java.util.List;

public class AuthDtos {

    // 로그인 요청
    public record LoginRequest(String username, String password) {}

    // 로그인 응답
    public record LoginResponse(String accessToken,
                                String refreshToken,
                                String accessJti,
                                String refreshJti,
                                long   accessTtlSeconds,
                                long   refreshTtlSeconds,
                                List<String> roles) {}

    // 리프레시 요청 (쿠키로 받으면 필드 조정)
    public record RefreshRequest(String refreshToken, String refreshJti, String userId) {}

    // 리프레시 응답
    public record RefreshResponse(String accessToken,
                                  String refreshToken,
                                  String accessJti,
                                  String refreshJti,
                                  long   accessTtlSeconds,
                                  long   refreshTtlSeconds) {}
}
