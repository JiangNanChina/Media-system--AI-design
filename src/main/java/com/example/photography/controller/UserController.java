package com.example.photography.controller;

import com.example.photography.dto.request.BatchUserRequest;
import com.example.photography.dto.request.UserCreateRequest;
import com.example.photography.dto.request.UserUpdateRequest;
import com.example.photography.dto.request.UserProfileUpdateRequest;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.UserResponse;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.UserRole;
import com.example.photography.service.EmailVerificationService;
import com.example.photography.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
@Tag(name = "用户管理", description = "用户的增删改查、头像上传、密码管理等操作")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private EmailVerificationService emailVerificationService;
    
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "获取所有用户列表", description = "获取所有用户列表（仅管理员）")
    public ApiResponse<List<UserResponse>> getAllUsers() {
        try {
            List<User> users = userService.findAllUsersWithDepartment();
            
            // 转换为UserResponse DTO
            List<UserResponse> userResponses = users.stream()
                .map(user -> new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAvatarUrl(),
                    user.getRole(),
                    user.getEnabled(),
                    user.getDepartment() != null ? user.getDepartment().getName() : null,
                    user.getDepartment() != null ? user.getDepartment().getId() : null,
                    user.getCreatedAt(),
                    user.getUpdatedAt()
                ))
                .collect(Collectors.toList());
            
            return ApiResponse.success(userResponses);
        } catch (Exception e) {
            return ApiResponse.error("获取用户列表失败: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "分页获取用户列表", description = "分页获取所有用户（仅管理员）")
    public ApiResponse<Page<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean enabled) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<User> users;
            
            // 判断是否有任何筛选条件
            boolean hasFilters = (keyword != null && !keyword.trim().isEmpty()) || 
                                 role != null || 
                                 departmentId != null || 
                                 enabled != null;
            
            if (hasFilters) {
                // 使用组合条件搜索
                UserRole userRole = (role != null && !role.trim().isEmpty()) ? UserRole.valueOf(role.trim()) : null;
                String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
                users = userService.searchUsersWithFilters(searchKeyword, userRole, departmentId, enabled, pageable);
            } else {
                // 无筛选条件，返回所有用户
                users = userService.findUsersWithDepartment(pageable);
            }
            
            // 转换为UserResponse DTO
            Page<UserResponse> userResponses = users.map(user -> new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getEnabled(),
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getCreatedAt(),
                user.getUpdatedAt()
            ));
            
            return ApiResponse.success(userResponses);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "搜索用户", description = "根据关键字搜索用户（仅管理员）")
    public ApiResponse<Page<UserResponse>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.searchUsersWithDepartment(keyword, pageable);
            
            // 转换为UserResponse DTO
            Page<UserResponse> userResponses = users.map(user -> new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getEnabled(),
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getCreatedAt(),
                user.getUpdatedAt()
            ));
            
            return ApiResponse.success(userResponses);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户", description = "根据用户ID获取用户详细信息")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findByIdWithDepartment(id);
            UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getEnabled(),
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getCreatedAt(),
                user.getUpdatedAt()
            );
            return ApiResponse.success(userResponse);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "根据角色获取用户", description = "根据用户角色获取用户列表（仅管理员）")
    public ApiResponse<List<UserResponse>> getUsersByRole(@PathVariable UserRole role) {
        try {
            List<User> users = userService.findByRoleWithDepartment(role);
            List<UserResponse> userResponses = users.stream()
                .map(user -> new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAvatarUrl(),
                    user.getRole(),
                    user.getEnabled(),
                    user.getDepartment() != null ? user.getDepartment().getName() : null,
                    user.getDepartment() != null ? user.getDepartment().getId() : null,
                    user.getCreatedAt(),
                    user.getUpdatedAt()
                ))
                .collect(Collectors.toList());
            return ApiResponse.success(userResponses);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/department/{departmentId}")
    @Operation(summary = "根据部门获取用户", description = "根据部门ID获取用户列表")
    public ApiResponse<List<UserResponse>> getUsersByDepartment(@PathVariable Long departmentId) {
        try {
            List<User> users = userService.findByDepartmentWithDetails(departmentId);
            List<UserResponse> userResponses = users.stream()
                .map(user -> new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAvatarUrl(),
                    user.getRole(),
                    user.getEnabled(),
                    user.getDepartment() != null ? user.getDepartment().getName() : null,
                    user.getDepartment() != null ? user.getDepartment().getId() : null,
                    user.getCreatedAt(),
                    user.getUpdatedAt()
                ))
                .collect(Collectors.toList());
            return ApiResponse.success(userResponses);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "创建用户", description = "创建新用户（仅管理员）")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        try {
            User currentUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
            if (currentUser.getRole() != UserRole.SUPER_ADMIN && currentUser.getRole() != UserRole.ADMIN) {
                request.setRole(UserRole.MEMBER);
            } else if (request.getRole() == UserRole.ADMIN) {
                request.setRole(UserRole.SUPER_ADMIN);
            }
            User user = userService.createUser(request);
            // 重新加载用户以获取部门信息
            User userWithDepartment = userService.findByIdWithDepartment(user.getId());
            UserResponse userResponse = new UserResponse(
                userWithDepartment.getId(),
                userWithDepartment.getUsername(),
                userWithDepartment.getRealName(),
                userWithDepartment.getEmail(),
                userWithDepartment.getPhone(),
                userWithDepartment.getAvatarUrl(),
                userWithDepartment.getRole(),
                userWithDepartment.getEnabled(),
                userWithDepartment.getDepartment() != null ? userWithDepartment.getDepartment().getName() : null,
                userWithDepartment.getDepartment() != null ? userWithDepartment.getDepartment().getId() : null,
                userWithDepartment.getCreatedAt(),
                userWithDepartment.getUpdatedAt()
            );
            return ApiResponse.success("用户创建成功", userResponse);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息", description = "部分更新用户信息（管理员或本人）")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, 
                                               @RequestBody UserUpdateRequest request) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);
            
            // 权限检查：必须是管理员或者修改自己的信息
            boolean isSuperAdmin = currentUser.getRole() == UserRole.SUPER_ADMIN || currentUser.getRole() == UserRole.ADMIN;
            boolean isAdmin = isSuperAdmin || currentUser.getRole() == UserRole.DIRECTOR;
            boolean isOwner = currentUser.getId().equals(id);
            
            if (!isAdmin && !isOwner) {
                return ApiResponse.error("权限不足：只能修改自己的信息");
            }

            // 身份提升只能通过超级管理员接口执行，普通用户也不能借此接口修改自己的权限和状态。
            request.setRole(null);
            if (!isAdmin) {
                request.setDepartmentId(null);
                request.setEnabled(null);
                request.setPassword(null);
            }
            
            User user = userService.updateUserPartial(id, request);
            UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getEnabled(),
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getCreatedAt(),
                user.getUpdatedAt()
            );
            return ApiResponse.success("用户信息更新成功", userResponse);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "删除用户", description = "删除指定用户（仅管理员）")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ApiResponse.success("用户删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/batch-enable")
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "批量启用用户", description = "批量启用指定用户（仅管理员）")
    public ApiResponse<Void> batchEnableUsers(@Valid @RequestBody BatchUserRequest request) {
        try {
            userService.batchEnableUsers(request.getUserIds());
            return ApiResponse.success("批量启用成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/batch-disable")
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "批量禁用用户", description = "批量禁用指定用户（仅管理员）")
    public ApiResponse<Void> batchDisableUsers(@Valid @RequestBody BatchUserRequest request) {
        try {
            userService.batchDisableUsers(request.getUserIds());
            return ApiResponse.success("批量禁用成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/avatar")
    @Operation(summary = "上传用户头像", description = "上传用户头像（管理员或本人）")
    public ApiResponse<String> uploadAvatar(@PathVariable Long id, 
                                          @RequestParam("file") MultipartFile file) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);
            
            // 权限检查：必须是管理员或者修改自己的头像
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            boolean isOwner = currentUser.getId().equals(id);
            
            if (!isAdmin && !isOwner) {
                return ApiResponse.error("权限不足：只能修改自己的头像");
            }
            
            String avatarUrl = userService.uploadAvatar(id, file);
            return ApiResponse.success("头像上传成功", avatarUrl);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/change-password")
    @Operation(summary = "修改密码", description = "修改用户密码（管理员或本人）")
    public ApiResponse<Void> changePassword(@PathVariable Long id, 
                                          @RequestBody Map<String, String> passwords) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);
            
            // 权限检查：必须是管理员或者修改自己的密码
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            boolean isOwner = currentUser.getId().equals(id);
            
            if (!isAdmin && !isOwner) {
                return ApiResponse.error("权限不足：只能修改自己的密码");
            }
            
            String oldPassword = passwords.get("oldPassword");
            String newPassword = passwords.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                return ApiResponse.error("旧密码和新密码不能为空");
            }
            
            userService.changePassword(id, oldPassword, newPassword);
            return ApiResponse.success("密码修改成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "重置密码", description = "重置用户密码（仅管理员）")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, 
                                         @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("newPassword");
            if (newPassword == null) {
                return ApiResponse.error("新密码不能为空");
            }
            
            userService.resetPassword(id, newPassword);
            return ApiResponse.success("密码重置成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "启用/禁用用户", description = "启用或禁用用户账户（仅管理员）")
    public ApiResponse<Void> toggleUserStatus(@PathVariable Long id, 
                                            @RequestBody Map<String, Boolean> request) {
        try {
            Boolean enabled = request.get("enabled");
            if (enabled == null) {
                return ApiResponse.error("启用状态不能为空");
            }
            
            userService.toggleUserStatus(id, enabled);
            return ApiResponse.success(enabled ? "用户已启用" : "用户已禁用");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "获取用户统计信息", description = "获取用户统计数据（仅管理员）")
    public ApiResponse<UserService.UserStatistics> getUserStatistics() {
        try {
            UserService.UserStatistics statistics = userService.getUserStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/profile")
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的个人信息")
    public ApiResponse<UserResponse> getProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsernameWithDepartment(username);
            
            UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getEnabled(),
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getCreatedAt(),
                user.getUpdatedAt()
            );
            
            return ApiResponse.success(userResponse);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/profile")
    @Operation(summary = "更新个人信息", description = "更新当前登录用户的个人信息")
    public ApiResponse<UserResponse> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username);
            
            if (currentUser == null) {
                return ApiResponse.error("用户不存在");
            }
            
            String requestedEmail = normalizeEmail(request.getEmail());
            String currentEmail = normalizeEmail(currentUser.getEmail());
            boolean emailChanged = requestedEmail != null && !requestedEmail.equals(currentEmail);

            if (emailChanged) {
                if (userService.existsByEmail(requestedEmail)) {
                    return ApiResponse.error("该邮箱已被注册，请更换邮箱");
                }
                emailVerificationService.verifyRegisterCode(requestedEmail, request.getEmailCode());
            }

            // 更新用户信息（只更新允许用户自己修改的字段）
            if (request.getRealName() != null) {
                currentUser.setRealName(request.getRealName());
            }
            if (request.getEmail() != null) {
                currentUser.setEmail(requestedEmail);
            }
            if (request.getPhone() != null) {
                currentUser.setPhone(request.getPhone());
            }
            if (request.getAvatarUrl() != null) {
                currentUser.setAvatarUrl(request.getAvatarUrl());
            }
            
            // 保存更新后的用户信息
            userService.save(currentUser);
            
            // 重新获取完整用户信息（包含部门信息）
            User userWithDepartment = userService.findByUsernameWithDepartment(username);
            
            UserResponse userResponse = new UserResponse(
                userWithDepartment.getId(),
                userWithDepartment.getUsername(),
                userWithDepartment.getRealName(),
                userWithDepartment.getEmail(),
                userWithDepartment.getPhone(),
                userWithDepartment.getAvatarUrl(),
                userWithDepartment.getRole(),
                userWithDepartment.getEnabled(),
                userWithDepartment.getDepartment() != null ? userWithDepartment.getDepartment().getName() : null,
                userWithDepartment.getDepartment() != null ? userWithDepartment.getDepartment().getId() : null,
                userWithDepartment.getCreatedAt(),
                userWithDepartment.getUpdatedAt()
            );
            
            return ApiResponse.success("个人信息更新成功", userResponse);
        } catch (Exception e) {
            return ApiResponse.error("更新个人信息失败: " + e.getMessage());
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail.isEmpty()) {
            throw new RuntimeException("邮箱不能为空");
        }
        return normalizedEmail;
    }
    
    /**
     * 管理员获取用户列表（用于设备管理等）
     */
    @GetMapping("/admin/list")
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "管理员获取用户列表", description = "管理员获取所有用户列表，支持批量操作")
    public ApiResponse<List<UserResponse>> getAdminUserList(
            @RequestParam(defaultValue = "1000") int size) {
        try {
            List<User> users = userService.findAllUsersWithDepartment();
            System.out.println("=== Admin用户列表调试 ===");
            System.out.println("查询到的用户数量: " + users.size());
            
            // 如果指定了size限制，则取前size个
            if (size > 0 && users.size() > size) {
                users = users.subList(0, size);
            }
            
            List<UserResponse> userResponses = users.stream()
                .map(user -> new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAvatarUrl(),
                    user.getRole(),
                    user.getEnabled(),
                    user.getDepartment() != null ? user.getDepartment().getName() : null,
                    user.getDepartment() != null ? user.getDepartment().getId() : null,
                    user.getCreatedAt(),
                    user.getUpdatedAt()
                ))
                .collect(Collectors.toList());
            
            return ApiResponse.success(userResponses);
        } catch (Exception e) {
            return ApiResponse.error("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取简单用户列表（用于选择器）
     */
    @GetMapping("/simple")
    @Operation(summary = "获取简单用户列表", description = "获取用于选择器的简单用户信息列表")
    public ApiResponse<List<UserResponse>> getSimpleUsers() {
        try {
            List<User> users = userService.getAllActiveUsers();
            List<UserResponse> userResponses = users.stream()
                .map(user -> new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAvatarUrl(),
                    user.getRole(),
                    user.getEnabled(),
                    user.getDepartment() != null ? user.getDepartment().getName() : null,
                    user.getDepartment() != null ? user.getDepartment().getId() : null,
                    user.getCreatedAt(),
                    user.getUpdatedAt()
                ))
                .collect(Collectors.toList());
            return ApiResponse.success(userResponses);
        } catch (Exception e) {
            return ApiResponse.error("获取用户列表失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}/physical")
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "物理删除用户", description = "物理删除用户及其所有相关数据（仅管理员）")
    public ApiResponse<Void> physicalDeleteUser(@PathVariable Long id) {
        try {
            userService.physicalDeleteUser(id);
            return ApiResponse.success("用户及相关数据已彻底删除");
        } catch (Exception e) {
            return ApiResponse.error("物理删除用户失败: " + e.getMessage());
        }
    }
}
