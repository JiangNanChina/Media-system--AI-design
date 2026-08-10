package com.example.photography.service;

import com.example.photography.dto.request.PasswordResetRequest;
import com.example.photography.model.entity.User;
import com.example.photography.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {
    private final UserRepository userRepository;
    private final VerificationCodeService verificationCodeService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public void sendCode(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmailIgnoreCaseAndDeletedFalse(normalized).isPresent()) {
            verificationCodeService.send(normalized, VerificationCodeService.PASSWORD_RESET, "找回密码验证码");
        }
    }

    public void reset(PasswordResetRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        verificationCodeService.verify(email, VerificationCodeService.PASSWORD_RESET, request.getCode());
        User user = userRepository.findByEmailIgnoreCaseAndDeletedFalse(email)
                .orElseThrow(() -> new IllegalArgumentException("验证码无效或账号不存在"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setTokenVersion((user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        refreshTokenService.revokeAll(user.getId());
    }
}
