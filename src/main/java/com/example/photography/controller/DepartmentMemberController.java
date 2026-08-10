package com.example.photography.controller;

import com.example.photography.dto.response.ApiResponse;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.UserRole;
import com.example.photography.repository.UserRepository;
import com.example.photography.utils.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/department-members")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MINISTER','DIRECTOR','SUPER_ADMIN')")
public class DepartmentMemberController {
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        User current = requiredCurrentUser();
        List<User> users = current.getRole() == UserRole.MINISTER
                ? userRepository.findByDepartmentIdWithDepartment(requiredDepartment(current))
                : userRepository.findAll().stream().filter(user -> !Boolean.TRUE.equals(user.getDeleted())).toList();
        return ApiResponse.success(users.stream().map(this::summary).toList());
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody DepartmentMemberUpdateRequest request) {
        User current = requiredCurrentUser();
        User target = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("成员不存在"));
        if (current.getRole() == UserRole.MINISTER
                && (target.getDepartment() == null || !requiredDepartment(current).equals(target.getDepartment().getId()))) {
            throw new org.springframework.security.access.AccessDeniedException("只能管理本部门成员");
        }
        if (target.getRole() != UserRole.MEMBER) {
            throw new org.springframework.security.access.AccessDeniedException("部长不能修改管理角色");
        }
        if (StringUtils.hasText(request.getRealName())) target.setRealName(request.getRealName().trim());
        if (StringUtils.hasText(request.getEmail())) target.setEmail(request.getEmail().trim().toLowerCase());
        if (StringUtils.hasText(request.getPhone())) target.setPhone(request.getPhone().trim());
        if (request.getEnabled() != null) target.setEnabled(request.getEnabled());
        userRepository.save(target);
        return ApiResponse.success("部门成员信息已更新");
    }

    private User requiredCurrentUser() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) throw new IllegalArgumentException("请先登录");
        return user;
    }

    private Long requiredDepartment(User user) {
        if (user.getDepartment() == null) throw new IllegalArgumentException("当前账号未分配部门");
        return user.getDepartment().getId();
    }

    private Map<String, Object> summary(User user) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("id", user.getId()); result.put("username", user.getUsername());
        result.put("realName", user.getRealName()); result.put("email", user.getEmail());
        result.put("phone", user.getPhone()); result.put("role", user.getRole());
        result.put("enabled", user.getEnabled());
        result.put("departmentName", user.getDepartment() == null ? null : user.getDepartment().getName());
        return result;
    }

    @Data
    public static class DepartmentMemberUpdateRequest {
        private String realName;
        @Email private String email;
        private String phone;
        private Boolean enabled;
    }
}
