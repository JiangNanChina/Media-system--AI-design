package com.example.photography.controller;

import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.AttendanceStatisticsResponse;
import com.example.photography.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/attendance")
@Tag(name = "考勤统计", description = "考勤统计相关接口")
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;
    
    @GetMapping("/statistics")
    @Operation(summary = "获取考勤统计", description = "获取指定配置在指定日期的考勤统计")
    public ApiResponse<AttendanceStatisticsResponse> getAttendanceStatistics(
            @RequestParam Long configId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            log.info("获取考勤统计: configId={}, date={}", configId, date);
            AttendanceStatisticsResponse statistics = attendanceService.getAttendanceStatistics(configId, date);
            return ApiResponse.success("获取成功", statistics);
        } catch (Exception e) {
            log.error("获取考勤统计失败: configId={}, date={}", configId, date, e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/statistics/range")
    @Operation(summary = "获取考勤统计范围", description = "获取指定配置在指定日期范围内的考勤统计")
    public ApiResponse<List<AttendanceStatisticsResponse>> getAttendanceStatisticsRange(
            @RequestParam Long configId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.info("获取考勤统计范围: configId={}, startDate={}, endDate={}", configId, startDate, endDate);
            List<AttendanceStatisticsResponse> statistics = attendanceService.getAttendanceStatistics(configId, startDate, endDate);
            return ApiResponse.success("获取成功", statistics);
        } catch (Exception e) {
            log.error("获取考勤统计范围失败: configId={}, startDate={}, endDate={}", configId, startDate, endDate, e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/statistics/all")
    @Operation(summary = "获取所有配置考勤统计", description = "获取所有配置在指定日期的考勤统计")
    public ApiResponse<List<AttendanceStatisticsResponse>> getAllAttendanceStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            log.info("获取所有配置考勤统计: date={}", date);
            List<AttendanceStatisticsResponse> statistics = attendanceService.getAllAttendanceStatistics(date);
            return ApiResponse.success("获取成功", statistics);
        } catch (Exception e) {
            log.error("获取所有配置考勤统计失败: date={}", date, e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
