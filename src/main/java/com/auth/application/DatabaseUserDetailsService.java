package com.auth.application;

import com.auth.domain.User;
import com.auth.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userMapper.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (Boolean.FALSE.equals(u.getEnabled())) {
            throw new UsernameNotFoundException("User disabled or deleted: " + username);
        }

        Collection<? extends GrantedAuthority> authorities = loadAuthorities(u.getId());
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPassword())
                .authorities(authorities)
                .accountLocked(false)
                .accountExpired(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private List<SimpleGrantedAuthority> loadAuthorities(UUID userId) {
        try {
            List<String> roleNames = jdbcTemplate.query(
                    """
                    SELECT r.name
                    FROM roles r
                    JOIN user_roles ur ON ur.role_id = r.id
                    WHERE ur.user_id = ?::uuid
                    """,
                    (rs, rowNum) -> rs.getString("name"),
                    userId.toString()
            );
            return roleNames.stream()
                    .map(this::normalizeToSpringRole)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }
    }

    private String normalizeToSpringRole(String raw) {
        if (raw == null || raw.isBlank()) return "ROLE_USER";
        return raw.startsWith("ROLE_") ? raw : "ROLE_" + raw;
    }
}
