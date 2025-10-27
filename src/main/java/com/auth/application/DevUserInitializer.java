package com.auth.application;

import com.auth.domain.User;
import com.auth.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevUserInitializer implements ApplicationRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        // 1) 기본 롤 시드
        seedRoleIfNotExists("ROLE_USER");
        seedRoleIfNotExists("ROLE_ADMIN");

        // 2) 기본 사용자 생성 (존재하지 않을 때만)
        createUserIfAbsent("user", "user1234!", "ROLE_USER");
        createUserIfAbsent("admin", "admin1234!", "ROLE_ADMIN");
    }

    private void seedRoleIfNotExists(String roleName) {
        jdbcTemplate.update(
                "INSERT INTO roles(name) VALUES (?) ON CONFLICT (name) DO NOTHING",
                roleName
        );
    }

    private void createUserIfAbsent(String username, String rawPassword, String roleName) {
        var existing = userMapper.findByUsername(username);
        UUID userId;
        if (existing.isPresent()) {
            userId = existing.get().getId();
        } else {
            userId = UUID.randomUUID();
            User u = User.builder()
                    .id(userId)
                    .username(username)
                    .password(passwordEncoder.encode(rawPassword))
                    .enabled(true)
                    .build();
            userMapper.insert(u);
        }

        // user_roles 매핑 (중복 없이)
        // role_id 조회 후 매핑 삽입
        jdbcTemplate.update(
                """
                INSERT INTO user_roles(user_id, role_id)
                SELECT ?::uuid, r.id
                FROM roles r
                WHERE r.name = ?
                ON CONFLICT (user_id, role_id) DO NOTHING
                """,
                userId.toString(), roleName
        );
    }
}
