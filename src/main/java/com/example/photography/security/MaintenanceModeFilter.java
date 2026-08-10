package com.example.photography.security;

import com.example.photography.dto.response.ApiResponse;
import com.example.photography.service.MaintenanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class MaintenanceModeFilter extends OncePerRequestFilter {
    public static final String COOKIE_NAME = "maintenance_access";
    private final MaintenanceService maintenanceService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if (!maintenanceService.isEnabled() || isPublicPath(path) || "OPTIONS".equalsIgnoreCase(request.getMethod())
                || maintenanceService.verifyToken(readCookie(request))) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error("MAINTENANCE_MODE"));
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/landing/public")
                || path.startsWith("/maintenance/public")
                || path.startsWith("/site-config/public")
                || path.startsWith("/uploads/site/")
                || path.startsWith("/images/site/");
    }

    private String readCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue).findFirst().orElse(null);
    }
}
