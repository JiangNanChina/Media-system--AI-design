package com.example.photography.service.impl;

import com.example.photography.config.FileUploadConfig;
import com.example.photography.dto.request.UserCreateRequest;
import com.example.photography.dto.request.UserUpdateRequest;
import com.example.photography.model.entity.Department;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.UserRole;
import com.example.photography.repository.*;
import com.example.photography.service.UserService;
import com.example.photography.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 用户服务实现类
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private DutyRecordRepository dutyRecordRepository;
    
    @Autowired
    private DutyScheduleRepository dutyScheduleRepository;
    
    @Autowired
    private CheckinRecordRepository checkinRecordRepository;
    
    @Autowired
    private UserDeviceRepository userDeviceRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private FileUploadConfig fileUploadConfig;
    
    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsernameOrEmail(String account) {
        if (!StringUtils.hasText(account)) {
            throw new RuntimeException("用户不存在");
        }

        String normalizedAccount = account.trim();
        if (normalizedAccount.contains("@")) {
            String normalizedEmail = normalizedAccount.toLowerCase(Locale.ROOT);
            return userRepository.findByEmailIgnoreCaseAndDeletedFalse(normalizedEmail)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
        }

        return userRepository.findByUsernameAndDeletedFalse(normalizedAccount)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public User findByUsernameWithDepartment(String username) {
        // 使用带部门信息的查询，避免懒加载问题
        return userRepository.findByUsernameAndDeletedFalseWithDepartment(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .filter(user -> !user.getDeleted())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    @Override
    public User findByIdWithDepartment(Long id) {
        return userRepository.findById(id)
                .filter(user -> !user.getDeleted())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    @Override
    public User createUser(UserCreateRequest request) {
        // 检查用户名是否存在
        if (userRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否存在
        if (StringUtils.hasText(request.getEmail()) && 
            userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        


        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        user.setRole(request.getRole());
        
        // 设置部门
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .filter(dept -> !dept.getDeleted())
                    .orElseThrow(() -> new RuntimeException("部门不存在"));
            user.setDepartment(department);
        }
        
        return userRepository.save(user);
    }
    
    @Override
    public User updateUser(Long id, UserCreateRequest request) {
        User user = findById(id);
        
        // 检查用户名是否被其他用户使用
        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否被其他用户使用
        if (StringUtils.hasText(request.getEmail()) && 
            !request.getEmail().equals(user.getEmail()) &&
            userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        


        
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        user.setRole(request.getRole());
        
        // 更新密码（如果提供）
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        // 更新部门
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .filter(dept -> !dept.getDeleted())
                    .orElseThrow(() -> new RuntimeException("部门不存在"));
            user.setDepartment(department);
        } else {
            user.setDepartment(null);
        }
        
        return userRepository.save(user);
    }
    
    @Override
    public User updateUserPartial(Long id, UserUpdateRequest request) {
        User user = findById(id);
        
        // 只更新提供的字段
        if (StringUtils.hasText(request.getUsername())) {
            // 检查用户名是否被其他用户使用
            if (!user.getUsername().equals(request.getUsername()) && 
                userRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(request.getUsername());
        }
        
        if (StringUtils.hasText(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        
        if (StringUtils.hasText(request.getEmail())) {
            // 检查邮箱是否被其他用户使用
            if (!request.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
                throw new RuntimeException("邮箱已存在");
            }
            user.setEmail(request.getEmail());
        }
        
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        
        if (StringUtils.hasText(request.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        
        // 更新密码（如果提供）
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        // 更新部门
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .filter(dept -> !dept.getDeleted())
                    .orElseThrow(() -> new RuntimeException("部门不存在"));
            user.setDepartment(department);
        }
        
        return userRepository.save(user);
    }
    
    @Override
    public void deleteUser(Long id) {
        User user = findById(id);
        user.setDeleted(true);
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void physicalDeleteUser(Long id) {
        User user = findById(id);
        
        System.out.println("开始物理删除用户ID: " + id + ", 用户名: " + user.getUsername());
        
        // 1. 删除借还记录
        System.out.println("删除用户的借还记录...");
        borrowRecordRepository.deleteByUserId(id);
        borrowRecordRepository.deleteByApprovedById(id); // 删除该用户审批的记录
        
        // 2. 删除请假记录
        System.out.println("删除用户的请假记录...");
        leaveRequestRepository.deleteByUserId(id);
        leaveRequestRepository.deleteByApproverId(id); // 删除该用户审批的请假记录
        
        // 3. 删除值班安排记录（duty_schedules）
        System.out.println("删除用户的值班安排记录...");
        dutyScheduleRepository.deleteByUserId(id);
        
        // 4. 删除执勤记录
        System.out.println("删除用户的执勤记录...");
        dutyRecordRepository.deleteByUserId(id);
        
        // 5. 删除打卡记录
        System.out.println("删除用户的打卡记录...");
        checkinRecordRepository.deleteByUserId(id);
        
        // 6. 删除用户设备绑定
        System.out.println("删除用户的设备绑定...");
        userDeviceRepository.deleteByUserId(id);
        
        // 7. 删除用户头像文件（如果存在）
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                FileUploadUtil fileUploadUtil = new FileUploadUtil();
                fileUploadUtil.deleteFile(user.getAvatarUrl());
                System.out.println("已删除用户头像文件: " + user.getAvatarUrl());
            } catch (Exception e) {
                System.err.println("删除头像文件失败: " + e.getMessage());
                // 不抛出异常，继续删除流程
            }
        }
        
        // 8. 最后删除用户记录
        System.out.println("删除用户记录...");
        userRepository.delete(user);
        
        System.out.println("用户物理删除完成: " + user.getUsername());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<User> findUsers(Pageable pageable) {
        return userRepository.findByDeletedFalse(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<User> findUsersWithDepartment(Pageable pageable) {
        return userRepository.findByDeletedFalseWithDepartment(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsersWithDepartment() {
        return userRepository.findByDeletedFalseWithDepartmentList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsersWithDepartment(String keyword, Pageable pageable) {
        return userRepository.searchUsersWithDepartment(keyword, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsersWithFilters(String keyword, UserRole role, Long departmentId, Boolean enabled, Pageable pageable) {
        return userRepository.searchUsersWithFilters(keyword, role, departmentId, enabled, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRoleAndDeletedFalse(role);
    }
    
    @Override
    public List<User> findByRoleWithDepartment(UserRole role) {
        return userRepository.findByRoleWithDepartment(role);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> findByDepartment(Long departmentId) {
        return userRepository.findByDepartmentIdAndDeletedFalse(departmentId);
    }
    
    @Override
    public List<User> findByDepartmentWithDetails(Long departmentId) {
        return userRepository.findByDepartmentIdWithDepartment(departmentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return false;
            }
            return userRepository.existsByUsernameAndDeletedFalse(username.trim());
        } catch (Exception e) {
            System.err.println("检查用户名存在性时发生错误: username=" + username + ", error=" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return false;
            }
            return userRepository.existsByEmailAndDeletedFalse(email.trim());
        } catch (Exception e) {
            System.err.println("检查邮箱存在性时发生错误: email=" + email + ", error=" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
    
    
    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        User user = findById(userId);
        
        // 使用统一的文件上传工具
        FileUploadUtil fileUploadUtil = new FileUploadUtil();
        String avatarUrl = fileUploadUtil.uploadFile(file, "avatars");
        
        // 更新用户头像URL
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        
        return avatarUrl;
    }
    
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = findById(userId);
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码不正确");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    @Override
    public void resetPassword(Long userId, String newPassword) {
        User user = findById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    @Override
    public void resetPasswordToDefault(Long userId) {
        User user = findById(userId);
        String defaultPassword = "123456";
        user.setPassword(passwordEncoder.encode(defaultPassword));
        userRepository.save(user);
    }
    
    @Override
    public void toggleUserStatus(Long userId, boolean enabled) {
        User user = findById(userId);
        user.setEnabled(enabled);
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void batchEnableUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        
        List<User> users = userRepository.findAllById(userIds);
        users.forEach(user -> {
            if (!user.getDeleted()) {
                user.setEnabled(true);
            }
        });
        userRepository.saveAll(users);
    }
    
    @Override
    @Transactional
    public void batchDisableUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        
        List<User> users = userRepository.findAllById(userIds);
        users.forEach(user -> {
            if (!user.getDeleted()) {
                user.setEnabled(false);
            }
        });
        userRepository.saveAll(users);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics() {
        long totalUsers = userRepository.countByDeletedFalse();
        long adminCount = userRepository.countByRoleAndDeletedFalse(UserRole.ADMIN);
        long memberCount = userRepository.countByRoleAndDeletedFalse(UserRole.MEMBER);
        
        // 这里可以添加更复杂的活跃用户统计逻辑
        long activeUsers = userRepository.countByDeletedFalse(); // 暂时用总用户数
        
        return new UserStatistics(totalUsers, adminCount, memberCount, activeUsers);
    }
    
    @Override
    public List<User> getAllActiveUsers() {
        return userRepository.findByEnabledTrueAndDeletedFalseOrderByRealNameAsc();
    }
}
