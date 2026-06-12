package com.example.photography.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日打卡汇总响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "每日打卡汇总信息")
public class DailyCheckinSummaryResponse {
    
    @Schema(description = "日期", example = "2024-09-06")
    private LocalDate date;
    
    @Schema(description = "应签到总人数", example = "50")
    private Integer totalRequiredCount;
    
    @Schema(description = "已签到人数", example = "45")
    private Integer checkedInCount;
    
    @Schema(description = "签到率", example = "0.90")
    private Double checkinRate;
    
    @Schema(description = "迟到人数", example = "5")
    private Integer lateCount;
    
    @Schema(description = "请假人数", example = "2")
    private Integer leaveCount;
    
    @Schema(description = "缺勤人数", example = "3")
    private Integer absentCount;
    
    @Schema(description = "待审核人数", example = "2")
    private Integer pendingAuditCount;
    
    @Schema(description = "活跃配置数量", example = "3")
    private Integer activeConfigCount;
    
    @Schema(description = "主要地点名称", example = "图书馆")
    private String mainLocationName;
    
    @Schema(description = "主要时段名称", example = "晚自习")
    private String mainSessionName;
    
    @Schema(description = "配置名称", example = "图书馆晚自习")
    private String configurationName;
    
    @Schema(description = "当日具体打卡记录列表")
    private List<CheckinRecordDetailResponse> records;
    
    @Schema(description = "用户考勤状态列表")
    private List<UserAttendanceStatusResponse> userStatuses;
    
    /**
     * 计算签到率：只考虑已签到人数与缺勤人数
     */
    public void calculateCheckinRate() {
        int signedCount = checkedInCount != null ? checkedInCount : 0;
        int absent = absentCount != null ? absentCount : 0;
        int denominator = signedCount + absent;

        if (denominator > 0) {
            this.checkinRate = (double) signedCount / denominator;
        } else {
            this.checkinRate = 0.0;
        }
    }
}
