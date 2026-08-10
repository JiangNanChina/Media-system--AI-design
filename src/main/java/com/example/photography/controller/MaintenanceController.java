package com.example.photography.controller;

import com.example.photography.dto.request.MaintenanceUnlockRequest;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.security.MaintenanceModeFilter;
import com.example.photography.service.MaintenanceService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {
    private final MaintenanceService service;

    @org.springframework.beans.factory.annotation.Value("${app.security.cookie-secure:true}")
    private boolean cookieSecure;

    @GetMapping("/public/status")
    public ApiResponse<Map<String, Object>> status(HttpServletRequest request) {
        return ApiResponse.success(service.publicStatus(cookie(request)));
    }

    @PostMapping("/public/unlock")
    public ResponseEntity<ApiResponse<Void>> unlock(@Valid @RequestBody MaintenanceUnlockRequest body,
                                                     HttpServletRequest request) {
        String token = service.unlock(body.getPassword(), clientKey(request));
        ResponseCookie cookie = ResponseCookie.from(MaintenanceModeFilter.COOKIE_NAME, token)
                .httpOnly(true).secure(cookieSecure).sameSite("Lax").path("/api")
                .maxAge(Duration.ofHours(2)).build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("维护通行验证成功"));
    }

    @PutMapping("/admin/settings")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> settings(@RequestBody MaintenanceSettingsRequest request) {
        return ApiResponse.success("维护设置已保存",
                service.saveSettings(request.getEnabled(), request.getPassword(), request.getTitle(), request.getMessage()));
    }

    private String cookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies()).filter(c -> MaintenanceModeFilter.COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue).findFirst().orElse(null);
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return StringUtils.hasText(forwarded) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    @Data
    public static class MaintenanceSettingsRequest {
        private Boolean enabled;
        private String password;
        private String title;
        private String message;
    }
}
