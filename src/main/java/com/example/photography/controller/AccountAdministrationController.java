package com.example.photography.controller;

import com.example.photography.dto.response.ApiResponse;
import com.example.photography.model.entity.Department;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.AccountStatus;
import com.example.photography.model.enums.UserRole;
import com.example.photography.repository.DepartmentRepository;
import com.example.photography.repository.UserRepository;
import com.example.photography.service.RefreshTokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AccountAdministrationController {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RefreshTokenService refreshTokenService;

    @GetMapping("/pending")
    public ApiResponse<List<Map<String, Object>>> pending() {
        return ApiResponse.success(userRepository.findAll().stream()
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()) && user.getAccountStatus() == AccountStatus.PENDING)
                .map(this::accountSummary)
                .toList());
    }

    @PutMapping("/{id}/review")
    @Transactional
    public ApiResponse<Void> review(@PathVariable Long id, @Valid @RequestBody AccountReviewRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (request.getApproved() && request.getDepartmentId() == null) {
            throw new IllegalArgumentException("审核通过前必须分配部门");
        }
        user.setAccountStatus(request.getApproved() ? AccountStatus.ACTIVE : AccountStatus.REJECTED);
        user.setEnabled(request.getApproved());
        if (request.getApproved() && request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("部门不存在"));
            user.setDepartment(department);
        }
        userRepository.save(user);
        return ApiResponse.success(request.getApproved() ? "账号已启用" : "注册申请已驳回");
    }

    @PutMapping("/{id}/role")
    @Transactional
    public ApiResponse<Void> role(@PathVariable Long id, @Valid @RequestBody RoleAssignmentRequest request) {
        if (request.getRole() == UserRole.ADMIN) throw new IllegalArgumentException("请使用SUPER_ADMIN身份");
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setRole(request.getRole());
        user.setTokenVersion((user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1);
        userRepository.save(user);
        refreshTokenService.revokeAll(id);
        return ApiResponse.success("用户身份已更新");
    }

    private Map<String, Object> accountSummary(User user) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("email", user.getEmail());
        result.put("role", user.getRole());
        result.put("accountStatus", user.getAccountStatus());
        return result;
    }

    @Data
    public static class AccountReviewRequest {
        @NotNull private Boolean approved;
        private Long departmentId;
    }

    @Data
    public static class RoleAssignmentRequest {
        @NotNull private UserRole role;
    }
}
