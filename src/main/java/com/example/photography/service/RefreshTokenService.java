package com.example.photography.service;

import com.example.photography.model.entity.RefreshToken;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.AccountStatus;
import com.example.photography.repository.RefreshTokenRepository;
import com.example.photography.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {
    private final RefreshTokenRepository repository;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    public IssuedRefreshToken issue(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        String raw = generateRaw();
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hash(raw));
        token.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));
        repository.save(token);
        return new IssuedRefreshToken(raw, user);
    }

    public IssuedRefreshToken rotate(String raw) {
        RefreshToken current = repository.findByTokenHashAndRevokedAtIsNullAndDeletedFalse(hash(raw))
                .orElseThrow(() -> new IllegalArgumentException("刷新令牌无效"));
        if (current.getExpiresAt().isBefore(LocalDateTime.now())) {
            current.setRevokedAt(LocalDateTime.now());
            repository.save(current);
            throw new IllegalArgumentException("刷新令牌已过期");
        }
        User user = current.getUser();
        if (!Boolean.TRUE.equals(user.getEnabled()) || user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("账号不可用");
        }
        String replacement = generateRaw();
        current.setRevokedAt(LocalDateTime.now());
        current.setReplacedByHash(hash(replacement));
        repository.save(current);
        RefreshToken next = new RefreshToken();
        next.setUser(user);
        next.setTokenHash(hash(replacement));
        next.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));
        repository.save(next);
        return new IssuedRefreshToken(replacement, user);
    }

    public void revoke(String raw) {
        if (raw == null || raw.isBlank()) return;
        repository.findByTokenHashAndRevokedAtIsNullAndDeletedFalse(hash(raw)).ifPresent(token -> {
            token.setRevokedAt(LocalDateTime.now());
            repository.save(token);
        });
    }

    public void revokeAll(Long userId) {
        repository.revokeAllForUser(userId, LocalDateTime.now());
    }

    private String generateRaw() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("无法处理刷新令牌", e);
        }
    }

    public record IssuedRefreshToken(String rawToken, User user) { }
}
