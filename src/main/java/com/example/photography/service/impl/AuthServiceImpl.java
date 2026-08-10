package com.example.photography.service.impl;

import com.example.photography.dto.request.LoginRequest;
import com.example.photography.dto.request.RegisterRequest;
import com.example.photography.dto.response.LoginResponse;
import com.example.photography.dto.response.RegisterResponse;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.AccountStatus;
import com.example.photography.model.enums.UserRole;
import com.example.photography.service.*;
import com.example.photography.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDeviceService userDeviceService;
    private final EmailVerificationService emailVerificationService;

    @Override
    public LoginResponse login(LoginRequest request) {
        return login(request, null, null);
    }

    @Override
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userService.findByUsernameOrEmail(request.getUsername());
        ensureCanLogin(user);

        LocalDateTime now = LocalDateTime.now();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new RuntimeException("登录失败次数过多，请15分钟后重试");
        }
        if (!verifyPassword(user, request.getPassword())) {
            int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                user.setLockedUntil(now.plusMinutes(15));
                user.setFailedLoginAttempts(0);
            }
            userService.save(user);
            throw new RuntimeException("用户名或密码错误");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userService.save(user);

        if (isOperationalRole(user.getRole()) && request.getDeviceInfo() != null) {
            UserDeviceService.DeviceValidationResult result = userDeviceService
                    .validateAndBindDevice(user, request.getDeviceInfo(), ipAddress, userAgent);
            if (!result.isSuccess()) {
                throw new RuntimeException(result.getMessage());
            }
        }

        LoginResponse response = buildResponse(user, true);
        response.setLoginSessionId(UUID.randomUUID().toString());
        return response;
    }

    @Override
    public LoginResponse refreshToken(String token) {
        if (!validateToken(token)) {
            throw new RuntimeException("令牌无效或已过期");
        }
        return issueAccessToken(jwtUtil.getUserIdFromToken(token));
    }

    @Override
    public LoginResponse issueAccessToken(Long userId) {
        User user = userService.findByIdWithDepartment(userId);
        ensureCanLogin(user);
        return buildResponse(user, true);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            return user.getAccountStatus() == AccountStatus.ACTIVE
                    && Boolean.TRUE.equals(user.getEnabled())
                    && Objects.equals(jwtUtil.getTokenVersionFromToken(token), normalizedVersion(user))
                    && jwtUtil.validateToken(token, username);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("请先登录");
        }
        User user = userService.findByUsernameWithDepartment(authentication.getName());
        ensureCanLogin(user);
        return buildResponse(user, false);
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (!Objects.equals(request.getPassword(), request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        if (userService.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (userService.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        emailVerificationService.verifyRegisterCode(request.getEmail(), request.getEmailCode());

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setRole(UserRole.MEMBER);
        user.setAccountStatus(AccountStatus.PENDING);
        user.setEnabled(false);
        User saved = userService.save(user);
        return new RegisterResponse(saved.getId(), saved.getUsername(), saved.getRealName(), saved.getEmail(),
                saved.getRole(), saved.getDepartment() == null ? null : saved.getDepartment().getName(),
                "注册申请已提交，请等待管理员审核");
    }

    private LoginResponse buildResponse(User user, boolean includeToken) {
        String token = includeToken ? jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId(), normalizedVersion(user)) : null;
        LoginResponse response = new LoginResponse(token, user.getUsername(), user.getRealName(), user.getEmail(),
                user.getRole(), user.getId(), user.getDepartment() == null ? null : user.getDepartment().getName(),
                user.getAvatarUrl(), user.getCreatedAt());
        response.setAccountStatus(user.getAccountStatus());
        return response;
    }

    private void ensureCanLogin(User user) {
        AccountStatus status = user.getAccountStatus() == null ? AccountStatus.ACTIVE : user.getAccountStatus();
        if (status == AccountStatus.PENDING) {
            throw new RuntimeException("账号正在审核中，请等待管理员启用");
        }
        if (status != AccountStatus.ACTIVE || !Boolean.TRUE.equals(user.getEnabled())) {
            throw new RuntimeException("账号已停用，请联系管理员");
        }
    }

    private boolean verifyPassword(User user, String rawPassword) {
        String stored = user.getPassword();
        return StringUtils.hasText(stored) && stored.startsWith("$2") && passwordEncoder.matches(rawPassword, stored);
    }

    private int normalizedVersion(User user) {
        return user.getTokenVersion() == null ? 0 : user.getTokenVersion();
    }

    private boolean isOperationalRole(UserRole role) {
        return role == UserRole.MEMBER || role == UserRole.MINISTER || role == UserRole.DIRECTOR;
    }
}
