package com.auth.application;

import com.auth.infrastructure.token.TokenStore;
import com.auth.interfaces.dto.AuthDtos.LoginRequest;
import com.auth.interfaces.dto.AuthDtos.LoginResponse;
import com.auth.interfaces.dto.AuthDtos.RefreshRequest;
import com.auth.interfaces.dto.AuthDtos.RefreshResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtIssuer jwtIssuer;
    private final JwtHelper jwtHelper;
    private final TokenStore tokenStore;

    @Value("${security.jwt.access-ttl-seconds:3600}")
    private long accessTtlSeconds;

    @Value("${security.jwt.refresh-ttl-seconds:1209600}") // 14 days
    private long refreshTtlSeconds;

    /** 6단계: 로그인 & 토큰 발급 + Refresh 저장 */
    public LoginResponse login(LoginRequest req) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        var principal = (UserDetails) auth.getPrincipal();
        String userId = principal.getUsername(); // 필요 시 UserDetails 커스텀해 실제 ID 사용

        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        var now = Instant.now();
        String accessJti  = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();

        String access  = jwtIssuer.issueAccessToken(userId, roles, accessJti, now, accessTtlSeconds);
        String refresh = jwtIssuer.issueRefreshToken(userId, refreshJti, now, refreshTtlSeconds);

        // Refresh 토큰을 Redis에 저장(원문 보관; 운영에선 해시 권장)
        tokenStore.saveRefreshToken(userId, refreshJti, refresh, refreshTtlSeconds);

        return new LoginResponse(access, refresh, accessJti, refreshJti,
                accessTtlSeconds, refreshTtlSeconds, roles);
    }

    /** 7단계: 리프레시(검증→회전) */
    public RefreshResponse refresh(RefreshRequest req) {
        // 1) Redis에서 기존 refresh 조회
        String saved = tokenStore.getRefreshToken(req.userId(), req.refreshJti());
        if (saved == null || !saved.equals(req.refreshToken())) {
            throw new IllegalArgumentException("Invalid/rotated refresh");
        }

        // 2) Refresh 토큰 자체 서명/만료 검증
        var parsed = jwtHelper.parseAndVerify(req.refreshToken());
        if (!parsed.subject().equals(req.userId())) {
            throw new IllegalArgumentException("Subject mismatch");
        }
        var now = Instant.now();
        long refreshLeft = jwtHelper.secondsLeft(parsed.expiresAt(), now);
        if (refreshLeft <= 0) {
            tokenStore.deleteRefreshToken(req.userId(), req.refreshJti());
            throw new IllegalArgumentException("Refresh expired");
        }

        // 3) 새 토큰들 발급 (회전)
        String newAccessJti  = UUID.randomUUID().toString();
        String newRefreshJti = UUID.randomUUID().toString();

        // 권한이 필요하면 DB/UserDetails 재조회 or 토큰에 포함한 최소정보 사용
        // 여기선 간단히 roles 없이 access 발급 → 실제론 사용자 로드로 roles 채워주세요
        // (또는 saved refresh에서 subject만 신뢰하고 roles는 다시 읽기)
        List<String> roles = List.of("ROLE_USER"); // TODO: 실제 로직으로 교체

        String newAccess  = jwtIssuer.issueAccessToken(req.userId(), roles, newAccessJti, now, accessTtlSeconds);
        String newRefresh = jwtIssuer.issueRefreshToken(req.userId(), newRefreshJti, now, refreshTtlSeconds);

        // 4) 기존 refresh 삭제, 새 refresh 저장
        tokenStore.deleteRefreshToken(req.userId(), req.refreshJti());
        tokenStore.saveRefreshToken(req.userId(), newRefreshJti, newRefresh, refreshTtlSeconds);

        return new RefreshResponse(newAccess, newRefresh, newAccessJti, newRefreshJti,
                accessTtlSeconds, refreshTtlSeconds);
    }
}
