package com.example.photography.service;

import com.example.photography.dto.response.AttendanceStatisticsResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * 考勤服务接口
 */
public interface AttendanceService {
    
    /**
     * 获取指定配置在指定日期的考勤统计
     * 
     * @param configId 打卡配置ID
     * @param date 统计日期
     * @return 考勤统计结果
     */
    AttendanceStatisticsResponse getAttendanceStatistics(Long configId, LocalDate date);
    
    /**
     * 获取指定配置在指定日期范围内的考勤统计列表
     * 
     * @param configId 打卡配置ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 考勤统计列表
     */
    List<AttendanceStatisticsResponse> getAttendanceStatistics(Long configId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取所有配置在指定日期的考勤统计汇总
     * 
     * @param date 统计日期
     * @return 所有配置的考勤统计列表
     */
    List<AttendanceStatisticsResponse> getAllAttendanceStatistics(LocalDate date);
}
