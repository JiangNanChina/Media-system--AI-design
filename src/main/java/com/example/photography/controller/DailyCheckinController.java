package com.example.photography.controller;

import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.DailyCheckinSummaryResponse;
import com.example.photography.service.DailyCheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 每日打卡汇总控制器
 */
@Slf4j
@RestController
@RequestMapping("/daily-checkin")
@RequiredArgsConstructor
@Tag(name = "每日打卡汇总", description = "每日打卡汇总相关接口")
public class DailyCheckinController {
    
    private final DailyCheckinService dailyCheckinService;
    
    /**
     * 获取每日打卡汇总列表（管理员）
     */
    @GetMapping("/summaries")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取每日打卡汇总列表", description = "管理员获取每日打卡汇总列表（仅管理员）")
    public ApiResponse<Page<DailyCheckinSummaryResponse>> getDailyCheckinSummaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.info("管理员获取每日打卡汇总: page={}, size={}, startDate={}, endDate={}", page, size, startDate, endDate);
            
            // 设置默认日期范围（最近30天）
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
            Page<DailyCheckinSummaryResponse> summaries = dailyCheckinService.getDailyCheckinSummaries(startDate, endDate, pageable);
            
            return ApiResponse.success(summaries);
        } catch (Exception e) {
            log.error("获取每日打卡汇总失败", e);
            return ApiResponse.error("获取每日打卡汇总失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户每日打卡汇总列表
     */
    @GetMapping("/user-summaries")
    @Operation(summary = "获取用户每日打卡汇总列表", description = "获取当前用户的每日打卡汇总列表")
    public ApiResponse<Page<DailyCheckinSummaryResponse>> getUserDailyCheckinSummaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // 获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getDetails() == null) {
                return ApiResponse.error("用户未认证");
            }
            
            Long userId = (Long) authentication.getDetails();
            log.info("用户获取每日打卡汇总: userId={}, page={}, size={}, startDate={}, endDate={}", 
                userId, page, size, startDate, endDate);
            
            // 设置默认日期范围（最近30天）
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
            Page<DailyCheckinSummaryResponse> summaries = dailyCheckinService.getUserDailyCheckinSummaries(userId, startDate, endDate, pageable);
            
            return ApiResponse.success(summaries);
        } catch (Exception e) {
            log.error("获取用户每日打卡汇总失败", e);
            return ApiResponse.error("获取用户每日打卡汇总失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取指定日期的详细打卡汇总
     */
    @GetMapping("/detail/{date}")
    @Operation(summary = "获取指定日期的详细打卡汇总", description = "获取指定日期的详细打卡汇总信息")
    public ApiResponse<DailyCheckinSummaryResponse> getDailyCheckinDetail(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getDetails() == null) {
                return ApiResponse.error("用户未认证");
            }
            
            Long userId = (Long) authentication.getDetails();
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            
            log.info("获取每日打卡详情: date={}, userId={}, isAdmin={}", date, userId, isAdmin);
            
            DailyCheckinSummaryResponse summary = dailyCheckinService.getDailyCheckinDetail(date, isAdmin, userId);
            
            return ApiResponse.success(summary);
        } catch (Exception e) {
            log.error("获取每日打卡详情失败", e);
            return ApiResponse.error("获取每日打卡详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除指定日期的所有打卡记录
     */
    @DeleteMapping("/delete-daily-records")
    @Operation(summary = "删除指定日期的所有打卡记录", description = "删除指定日期的所有打卡记录，仅管理员可操作")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteDailyRecords(@RequestParam String date) {
        try {
            LocalDate targetDate = LocalDate.parse(date);
            log.info("管理员删除每日打卡记录: date={}", targetDate);
            
            boolean deleted = dailyCheckinService.deleteDailyRecords(targetDate);
            if (deleted) {
                return ApiResponse.success("删除成功");
            } else {
                return ApiResponse.error("删除失败，未找到相关记录");
            }
        } catch (Exception e) {
            log.error("删除每日打卡记录失败", e);
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出指定日期的打卡汇总Excel
     */
    @GetMapping("/export")
    @Operation(summary = "导出每日打卡汇总Excel", description = "导出指定日期的所有用户打卡详细信息")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportDailyCheckinData(@RequestParam String date) {
        try {
            LocalDate targetDate = LocalDate.parse(date);
            log.info("导出每日打卡数据: date={}", targetDate);
            
            // 获取详细数据（管理员权限已通过@PreAuthorize验证）
            DailyCheckinSummaryResponse summary = dailyCheckinService.getDailyCheckinDetail(targetDate, true, null);
            
            if (summary == null || summary.getUserStatuses() == null || summary.getUserStatuses().isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            // 生成Excel文件
            byte[] excelData = dailyCheckinService.exportDailyCheckinToExcel(summary);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                "每日打卡汇总_" + date + ".xlsx");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
                
        } catch (Exception e) {
            log.error("导出每日打卡数据失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
