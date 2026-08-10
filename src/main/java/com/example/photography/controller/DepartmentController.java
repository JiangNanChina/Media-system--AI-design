package com.example.photography.controller;

import com.example.photography.dto.request.DepartmentCreateRequest;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.DepartmentResponse;
import com.example.photography.model.entity.Department;
import com.example.photography.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 */
@RestController
@RequestMapping("/departments")
@Tag(name = "部门管理", description = "部门的增删改查操作")
public class DepartmentController {
    
    @Autowired
    private DepartmentService departmentService;
    
    @GetMapping
    @Operation(summary = "获取所有部门", description = "获取系统中所有未删除的部门列表")
    public ApiResponse<Page<DepartmentResponse>> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            Page<DepartmentResponse> departments = departmentService.findAllDepartmentsWithUserCount(pageable);
            return ApiResponse.success(departments);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取部门", description = "根据部门ID获取部门详细信息")
    public ApiResponse<Department> getDepartmentById(@PathVariable Long id) {
        try {
            Department department = departmentService.findById(id);
            return ApiResponse.success(department);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索部门", description = "根据关键字搜索部门")
    public ApiResponse<Page<DepartmentResponse>> searchDepartments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            Page<DepartmentResponse> departments = departmentService.searchDepartmentsWithUserCount(keyword, pageable);
            return ApiResponse.success(departments);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/list")
    @Operation(summary = "获取部门列表", description = "获取简单的部门列表（用于下拉选择等）")
    public ApiResponse<List<DepartmentResponse>> getDepartmentList() {
        try {
            List<DepartmentResponse> departments = departmentService.findAllDepartmentsResponse();
            return ApiResponse.success(departments);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "创建部门", description = "创建新的部门（仅管理员）")
    public ApiResponse<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentCreateRequest request) {
        try {
            DepartmentResponse department = departmentService.createDepartmentResponse(request);
            return ApiResponse.success("部门创建成功", department);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "更新部门", description = "更新部门信息（仅管理员）")
    public ApiResponse<Department> updateDepartment(@PathVariable Long id, 
                                                   @Valid @RequestBody DepartmentCreateRequest request) {
        try {
            Department department = departmentService.updateDepartment(id, request);
            return ApiResponse.success("部门更新成功", department);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "删除部门", description = "删除指定部门（仅管理员）")
    public ApiResponse<Void> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return ApiResponse.success("部门删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "获取部门统计信息", description = "获取部门统计数据（仅管理员）")
    public ApiResponse<DepartmentService.DepartmentStatistics> getDepartmentStatistics() {
        try {
            DepartmentService.DepartmentStatistics statistics = departmentService.getDepartmentStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/init")
    @PreAuthorize("hasAnyRole('DIRECTOR','SUPER_ADMIN')")
    @Operation(summary = "初始化默认部门", description = "初始化系统默认部门（仅管理员）")
    public ApiResponse<Void> initializeDefaultDepartments() {
        try {
            departmentService.initializeDefaultDepartments();
            return ApiResponse.success("默认部门初始化成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
