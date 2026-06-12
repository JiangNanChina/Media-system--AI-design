package com.example.photography.security;

import com.example.photography.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String token = getTokenFromRequest(request);
        
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                System.out.println("JWT Filter - 开始处理 token: " + token.substring(0, Math.min(20, token.length())) + "...");
                
                String username = jwtUtil.getUsernameFromToken(token);
                System.out.println("JWT Filter - 获取用户名: " + username);
                
                String role = jwtUtil.getRoleFromToken(token);
                System.out.println("JWT Filter - 获取角色: " + role);
                
                Long userId = jwtUtil.getUserIdFromToken(token);
                System.out.println("JWT Filter - 获取用户ID: " + userId);
                
                if (jwtUtil.validateToken(token, username)) {
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            username, 
                            null, 
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                    
                    // 在认证对象中添加用户ID
                    authentication.setDetails(userId);
                    
                    System.out.println("JWT Filter - 成功设置认证，用户: " + username + ", 用户ID: " + userId + ", 角色: " + role);
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    System.out.println("JWT Filter - Token验证失败，用户: " + username);
                }
            } catch (Exception e) {
                System.out.println("JWT Filter - 设置用户认证失败: " + e.getMessage());
                e.printStackTrace();
                logger.error("无法设置用户认证: ", e);
            }
        } else if (token == null) {
            System.out.println("JWT Filter - 没有找到 token");
        } else {
            System.out.println("JWT Filter - 用户已认证，跳过处理");
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求中获取token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
