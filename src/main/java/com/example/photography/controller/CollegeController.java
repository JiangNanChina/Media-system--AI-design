package com.example.photography.controller;

import com.example.photography.dto.request.CollegeCreateRequest;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.CollegeResponse;
import com.example.photography.model.entity.College;
import com.example.photography.service.CollegeService;
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
 * 学院管理控制器。
 */
@RestController
@RequestMapping("/colleges")
@Tag(name = "学院管理", description = "学院的增删改查和下拉列表")
public class CollegeController {

    @Autowired
    private CollegeService collegeService;

    @GetMapping
    @Operation(summary = "分页获取学院", description = "获取学院分页列表")
    public ApiResponse<Page<CollegeResponse>> getAllColleges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            return ApiResponse.success(collegeService.findAllColleges(pageable));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取学院", description = "根据学院ID获取学院详情")
    public ApiResponse<College> getCollegeById(@PathVariable Long id) {
        try {
            return ApiResponse.success(collegeService.findById(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "搜索学院", description = "根据关键字搜索学院")
    public ApiResponse<Page<CollegeResponse>> searchColleges(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            return ApiResponse.success(collegeService.searchColleges(keyword, pageable));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "获取学院下拉列表", description = "获取所有学院，用于表单选择")
    public ApiResponse<List<CollegeResponse>> getCollegeList() {
        try {
            return ApiResponse.success(collegeService.findAllCollegeResponses());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建学院", description = "创建新的学院（管理员）")
    public ApiResponse<CollegeResponse> createCollege(@Valid @RequestBody CollegeCreateRequest request) {
        try {
            CollegeResponse college = collegeService.createCollege(request);
            return ApiResponse.success("学院创建成功", college);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新学院", description = "更新学院信息（管理员）")
    public ApiResponse<CollegeResponse> updateCollege(
            @PathVariable Long id,
            @Valid @RequestBody CollegeCreateRequest request) {
        try {
            CollegeResponse college = collegeService.updateCollege(id, request);
            return ApiResponse.success("学院更新成功", college);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除学院", description = "删除指定学院（管理员）")
    public ApiResponse<Void> deleteCollege(@PathVariable Long id) {
        try {
            collegeService.deleteCollege(id);
            return ApiResponse.success("学院删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
