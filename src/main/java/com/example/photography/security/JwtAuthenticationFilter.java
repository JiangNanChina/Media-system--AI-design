package com.example.photography.security;

import com.example.photography.model.entity.User;
import com.example.photography.model.enums.AccountStatus;
import com.example.photography.model.enums.UserRole;
import com.example.photography.repository.UserRepository;
import com.example.photography.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = bearerToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = jwtUtil.getUsernameFromToken(token);
                User user = userRepository.findByUsernameAndDeletedFalse(username).orElse(null);
                if (user != null && Boolean.TRUE.equals(user.getEnabled())
                        && (user.getAccountStatus() == null || user.getAccountStatus() == AccountStatus.ACTIVE)
                        && Objects.equals(jwtUtil.getTokenVersionFromToken(token), user.getTokenVersion() == null ? 0 : user.getTokenVersion())
                        && jwtUtil.validateToken(token, username)) {
                    Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
                    user.getEffectiveRoles().stream()
                            .map(UserRole::normalized)
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                            .forEach(authorities::add);
                    if (user.getEffectiveRoles().stream().anyMatch(UserRole::canManageBusiness)) {
                        // Legacy controllers use ROLE_ADMIN as the common business-management permission.
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    }
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);
                    authentication.setDetails(user.getId());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.debug("JWT认证失败: {}", e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }

    private String bearerToken(HttpServletRequest request) {
        String value = request.getHeader("Authorization");
        return value != null && value.startsWith("Bearer ") ? value.substring(7) : null;
    }
}
