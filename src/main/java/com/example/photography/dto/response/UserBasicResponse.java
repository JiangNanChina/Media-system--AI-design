package com.example.photography.dto.response;

import com.example.photography.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户基本信息响应DTO
 */
@Data
@Schema(description = "用户基本信息响应")
public class UserBasicResponse {
    
    @Schema(description = "用户ID")
    private Long id;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "真实姓名")
    private String realName;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "角色")
    private String role;
    
    /**
     * 从User实体转换为基本信息响应DTO
     * 安全处理 Hibernate 懒加载代理对象
     */
    public static UserBasicResponse fromUser(User user) {
        if (user == null) {
            return null;
        }
        
        try {
            // 尝试访问用户数据，如果是未初始化的代理会抛出异常
            UserBasicResponse response = new UserBasicResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setRealName(user.getRealName());
            response.setEmail(user.getEmail());
            response.setRole(user.getRole() != null ? user.getRole().toString() : null);
            
            return response;
        } catch (org.hibernate.LazyInitializationException e) {
            // 如果遇到懒加载异常，返回一个只包含ID的基本响应
            UserBasicResponse response = new UserBasicResponse();
            response.setId(user.getId());
            response.setUsername("用户" + user.getId());
            response.setRealName("未加载");
            return response;
        }
    }
}
