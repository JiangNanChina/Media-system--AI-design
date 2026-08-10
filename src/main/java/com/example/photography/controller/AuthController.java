package com.example.photography.controller;

import com.example.photography.dto.request.LoginRequest;
import com.example.photography.dto.request.EmailCodeRequest;
import com.example.photography.dto.request.RegisterRequest;
import com.example.photography.dto.request.PasswordResetRequest;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.LoginResponse;
import com.example.photography.dto.response.RegisterResponse;
import com.example.photography.service.AuthService;
import com.example.photography.service.EmailVerificationService;
import com.example.photography.service.UserService;
import com.example.photography.service.RefreshTokenService;
import com.example.photography.service.PasswordResetService;
import com.example.photography.service.VerificationRequestLimiter;
import com.example.photography.service.VerificationCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

import java.time.Duration;
import java.util.Arrays;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户登录、注册、令牌管理")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private VerificationRequestLimiter verificationRequestLimiter;

    @org.springframework.beans.factory.annotation.Value("${app.security.cookie-secure:true}")
    private boolean cookieSecure;
    
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名或邮箱和密码登录系统")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor,
            @RequestHeader(value = "X-Real-IP", required = false) String xRealIp,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            HttpServletRequest httpRequest) {
        try {
            // 获取真实IP地址
            String ipAddress = getClientIpAddress(httpRequest, xForwardedFor, xRealIp);
            
            LoginResponse response = authService.login(request, ipAddress, userAgent);
            RefreshTokenService.IssuedRefreshToken refresh = refreshTokenService.issue(response.getUserId());
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie(refresh.rawToken(), httpRequest).toString())
                    .body(ApiResponse.success("登录成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用现有令牌刷新获取新令牌")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(HttpServletRequest request) {
        try {
            String raw = readCookie(request, "refresh_token");
            RefreshTokenService.IssuedRefreshToken refresh = refreshTokenService.rotate(raw);
            LoginResponse response = authService.issueAccessToken(refresh.user().getId());
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie(refresh.rawToken(), request).toString())
                    .body(ApiResponse.success("令牌刷新成功", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        refreshTokenService.revoke(readCookie(request, "refresh_token"));
        ResponseCookie clear = ResponseCookie.from("refresh_token", "").httpOnly(true)
                .secure(cookieSecure).sameSite("Lax").path("/api/auth").maxAge(Duration.ZERO).build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, clear.toString()).body(ApiResponse.success("已退出登录"));
    }

    @PostMapping("/password-reset/email-code")
    public ApiResponse<Void> sendPasswordResetCode(@Valid @RequestBody EmailCodeRequest request,
                                                    HttpServletRequest httpRequest) {
        verificationRequestLimiter.check(request.getEmail(), VerificationCodeService.PASSWORD_RESET,
                getClientIpAddress(httpRequest, httpRequest.getHeader("X-Forwarded-For"), httpRequest.getHeader("X-Real-IP")));
        passwordResetService.sendCode(request.getEmail());
        return ApiResponse.success("如果该邮箱已注册，验证码将发送到邮箱");
    }

    @PostMapping("/password-reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.reset(request);
        return ApiResponse.success("密码已重置，请重新登录");
    }
    
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ApiResponse<LoginResponse> getCurrentUser() {
        try {
            LoginResponse response = authService.getCurrentUser();
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/validate")
    @Operation(summary = "验证令牌", description = "验证令牌是否有效")
    public ApiResponse<Boolean> validateToken(@RequestHeader("Authorization") String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return ApiResponse.success("令牌验证", false);
            }
            
            String token = authorization.substring(7);
            boolean isValid = authService.validateToken(token);
            return ApiResponse.success("令牌验证", isValid);
        } catch (Exception e) {
            return ApiResponse.success("令牌验证", false);
        }
    }
    
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户账户，支持成员和管理员注册")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = authService.register(request);
            return ApiResponse.success(response.getMessage(), response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/email-code")
    @Operation(summary = "发送注册邮箱验证码", description = "向注册邮箱发送6位验证码")
    public ApiResponse<Void> sendEmailCode(@Valid @RequestBody EmailCodeRequest request,
                                           HttpServletRequest httpRequest) {
        try {
            verificationRequestLimiter.check(request.getEmail(), "REGISTER",
                    getClientIpAddress(httpRequest, httpRequest.getHeader("X-Forwarded-For"), httpRequest.getHeader("X-Real-IP")));
            emailVerificationService.sendRegisterCode(request.getEmail());
            return ApiResponse.success("如邮箱可用于注册，验证码将发送到邮箱");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名是否存在", description = "检查用户名是否已被注册")
    public ApiResponse<Boolean> checkUsername(@RequestParam(required = false) String username) {
        try {
            // 参数校验
            if (username == null || username.trim().isEmpty()) {
                return ApiResponse.success("用户名检查", false);
            }
            
            // 去除首尾空格，限制长度
            username = username.trim();
            if (username.length() > 50) {
                return ApiResponse.success("用户名检查", false);
            }
            
            boolean exists = userService.existsByUsername(username);
            return ApiResponse.success("用户名检查", exists);
        } catch (Exception e) {
            // 记录错误但不暴露给前端，默认返回不存在
            System.err.println("检查用户名时发生错误: username=" + username + ", error=" + e.getMessage());
            e.printStackTrace();
            return ApiResponse.success("用户名检查", false);
        }
    }
    
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱是否存在", description = "检查邮箱是否已被注册")
    public ApiResponse<Boolean> checkEmail(@RequestParam(required = false) String email) {
        try {
            // 参数校验
            if (email == null || email.trim().isEmpty()) {
                return ApiResponse.success("邮箱检查", false);
            }
            
            // 去除首尾空格，限制长度
            email = email.trim();
            if (email.length() > 100) {
                return ApiResponse.success("邮箱检查", false);
            }
            
            boolean exists = userService.existsByEmail(email);
            return ApiResponse.success("邮箱检查", exists);
        } catch (Exception e) {
            // 记录错误但不暴露给前端，默认返回不存在
            System.err.println("检查邮箱时发生错误: email=" + email + ", error=" + e.getMessage());
            e.printStackTrace();
            return ApiResponse.success("邮箱检查", false);
        }
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request, String xForwardedFor, String xRealIp) {
        String ipAddress = null;
        
        // 优先使用X-Forwarded-For
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For可能包含多个IP，取第一个
            ipAddress = xForwardedFor.split(",")[0].trim();
        }
        
        // 其次使用X-Real-IP
        if ((ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) 
            && xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            ipAddress = xRealIp;
        }
        
        // 最后使用request.getRemoteAddr()
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // 处理IPv6本地地址
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }
        
        return ipAddress;
    }

    private ResponseCookie refreshCookie(String token, HttpServletRequest request) {
        return ResponseCookie.from("refresh_token", token).httpOnly(true).secure(cookieSecure)
                .sameSite("Lax").path("/api/auth").maxAge(Duration.ofDays(7)).build();
    }

    private String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies()).filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue).findFirst().orElse(null);
    }
}
