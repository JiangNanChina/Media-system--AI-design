package com.example.photography.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类测试
 * 验证JWT API更新后的功能
 */
public class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    @BeforeEach
    public void setUp() {
        jwtUtil = new JwtUtil();
        // 设置测试用的配置值
        ReflectionTestUtils.setField(jwtUtil, "secret", "mySecretKey123456789012345678901234567890123456789012345678901234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24小时
    }
    
    @Test
    public void testGenerateAndValidateToken() {
        String username = "testuser";
        String role = "ADMIN";
        Long userId = 1L;
        
        // 生成token
        String token = jwtUtil.generateToken(username, role, userId);
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // 验证token
        assertTrue(jwtUtil.validateToken(token, username));
        
        // 提取信息
        assertEquals(username, jwtUtil.getUsernameFromToken(token));
        assertEquals(role, jwtUtil.getRoleFromToken(token));
        assertEquals(userId, jwtUtil.getUserIdFromToken(token));
        
        // 检查是否过期
        assertFalse(jwtUtil.isTokenExpired(token));
    }
    
    @Test
    public void testRefreshToken() throws InterruptedException {
        String username = "testuser";
        String role = "MEMBER";
        Long userId = 2L;
        
        // 生成原始token
        String originalToken = jwtUtil.generateToken(username, role, userId);
        
        // 等待至少1秒，确保刷新后的token有不同的时间戳
        Thread.sleep(1000);
        
        // 刷新token
        String refreshedToken = jwtUtil.refreshToken(originalToken);
        assertNotNull(refreshedToken);
        assertNotEquals(originalToken, refreshedToken, "刷新后的token应该不同");
        
        // 验证刷新后的token包含相同的信息
        assertEquals(username, jwtUtil.getUsernameFromToken(refreshedToken));
        assertEquals(role, jwtUtil.getRoleFromToken(refreshedToken));
        assertEquals(userId, jwtUtil.getUserIdFromToken(refreshedToken));
    }
    
    @Test
    public void testInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        // 无效token应该验证失败
        assertFalse(jwtUtil.validateToken(invalidToken, "anyuser"));
        assertTrue(jwtUtil.isTokenExpired(invalidToken));
        assertNull(jwtUtil.refreshToken(invalidToken));
    }
}
