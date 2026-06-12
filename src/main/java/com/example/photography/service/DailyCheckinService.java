package com.example.photography.service;

import com.example.photography.dto.response.DailyCheckinSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * 每日打卡汇总服务接口
 */
public interface DailyCheckinService {
    
    /**
     * 获取每日打卡汇总列表（管理员）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageable 分页信息
     * @return 每日打卡汇总分页列表
     */
    Page<DailyCheckinSummaryResponse> getDailyCheckinSummaries(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * 获取用户的每日打卡汇总列表
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageable 分页信息
     * @return 用户每日打卡汇总分页列表
     */
    Page<DailyCheckinSummaryResponse> getUserDailyCheckinSummaries(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * 获取指定日期的详细打卡汇总
     * @param date 日期
     * @param isAdmin 是否为管理员
     * @param userId 用户ID（非管理员时使用）
     * @return 当日详细打卡汇总
     */
    DailyCheckinSummaryResponse getDailyCheckinDetail(LocalDate date, boolean isAdmin, Long userId);
    
    /**
     * 删除指定日期的所有打卡记录
     * @param date 日期
     * @return 是否删除成功
     */
    boolean deleteDailyRecords(LocalDate date);
    
    /**
     * 导出每日打卡汇总到Excel
     * @param summary 每日汇总数据
     * @return Excel文件字节数组
     */
    byte[] exportDailyCheckinToExcel(DailyCheckinSummaryResponse summary);
}
