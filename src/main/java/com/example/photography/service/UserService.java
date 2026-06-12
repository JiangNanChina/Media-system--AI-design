package com.example.photography.service;

import com.example.photography.dto.request.UserCreateRequest;
import com.example.photography.dto.request.UserUpdateRequest;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 根据用户名或邮箱查找用户
     */
    User findByUsernameOrEmail(String account);
    
    /**
     * 根据用户名查找用户（带部门信息）
     */
    User findByUsernameWithDepartment(String username);
    
    /**
     * 根据ID查找用户
     */
    User findById(Long id);
    
    /**
     * 根据ID查找用户（包含部门信息）
     */
    User findByIdWithDepartment(Long id);
    
    /**
     * 创建用户
     */
    User createUser(UserCreateRequest request);
    
    /**
     * 更新用户信息
     */
    User updateUser(Long id, UserCreateRequest request);
    
    /**
     * 部分更新用户信息
     */
    User updateUserPartial(Long id, UserUpdateRequest request);
    
    /**
     * 删除用户（软删除）
     */
    void deleteUser(Long id);
    
    /**
     * 物理删除用户及其相关数据
     */
    void physicalDeleteUser(Long id);
    
    /**
     * 分页查询用户
     */
    Page<User> findUsers(Pageable pageable);
    
    /**
     * 分页查询用户（带部门信息）
     */
    Page<User> findUsersWithDepartment(Pageable pageable);
    
    /**
     * 获取所有用户（带部门信息）
     */
    List<User> findAllUsersWithDepartment();
    
    /**
     * 搜索用户
     */
    Page<User> searchUsers(String keyword, Pageable pageable);
    
    /**
     * 搜索用户（带部门信息）
     */
    Page<User> searchUsersWithDepartment(String keyword, Pageable pageable);
    
    /**
     * 组合条件搜索用户（带部门信息）
     */
    Page<User> searchUsersWithFilters(String keyword, UserRole role, Long departmentId, Boolean enabled, Pageable pageable);
    
    /**
     * 根据角色查找用户
     */
    List<User> findByRole(UserRole role);
    
    /**
     * 根据角色查找用户（包含部门信息）
     */
    List<User> findByRoleWithDepartment(UserRole role);
    
    /**
     * 根据部门查找用户
     */
    List<User> findByDepartment(Long departmentId);
    
    /**
     * 根据部门查找用户（包含详细信息）
     */
    List<User> findByDepartmentWithDetails(Long departmentId);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 保存用户
     */
    User save(User user);
    
    /**
     * 更新用户头像
     */
    String uploadAvatar(Long userId, MultipartFile file);
    
    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 重置密码
     */
    void resetPassword(Long userId, String newPassword);
    
    /**
     * 重置密码为默认密码（123456）
     */
    void resetPasswordToDefault(Long userId);
    
    /**
     * 启用/禁用用户
     */
    void toggleUserStatus(Long userId, boolean enabled);
    
    /**
     * 批量启用用户
     */
    void batchEnableUsers(List<Long> userIds);
    
    /**
     * 批量禁用用户
     */
    void batchDisableUsers(List<Long> userIds);
    
    /**
     * 获取用户统计信息
     */
    UserStatistics getUserStatistics();
    
    /**
     * 获取所有激活用户（用于选择器）
     */
    List<User> getAllActiveUsers();
    
    /**
     * 用户统计信息类
     */
    class UserStatistics {
        private long totalUsers;
        private long adminCount;
        private long memberCount;
        private long activeUsers;
        
        // Constructors, getters and setters
        public UserStatistics() {}
        
        public UserStatistics(long totalUsers, long adminCount, long memberCount, long activeUsers) {
            this.totalUsers = totalUsers;
            this.adminCount = adminCount;
            this.memberCount = memberCount;
            this.activeUsers = activeUsers;
        }
        
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        
        public long getAdminCount() { return adminCount; }
        public void setAdminCount(long adminCount) { this.adminCount = adminCount; }
        
        public long getMemberCount() { return memberCount; }
        public void setMemberCount(long memberCount) { this.memberCount = memberCount; }
        
        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
    }
}
