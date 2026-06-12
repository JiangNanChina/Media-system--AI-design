package com.example.photography.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤统计响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceStatisticsResponse {
    
    /**
     * 配置ID
     */
    private Long configId;
    
    /**
     * 配置名称
     */
    private String configName;
    
    /**
     * 统计日期
     */
    private LocalDate statisticsDate;
    
    /**
     * 应到人数
     */
    private Integer requiredCount;
    
    /**
     * 实到人数
     */
    private Integer actualCount;
    
    /**
     * 请假人数
     */
    private Integer leaveCount;
    
    /**
     * 缺勤人数
     */
    private Integer absentCount;
    
    /**
     * 出勤率（百分比）
     */
    private Double attendanceRate;
    
    /**
     * 用户考勤详情列表
     */
    private List<UserAttendanceDetail> userDetails;
    
    /**
     * 用户考勤详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAttendanceDetail {
        
        /**
         * 用户ID
         */
        private Long userId;
        
        /**
         * 用户姓名
         */
        private String userName;
        
        /**
         * 部门名称
         */
        private String departmentName;
        
        /**
         * 考勤状态
         */
        private AttendanceStatus status;
        
        /**
         * 打卡时间
         */
        private LocalDateTime checkinTime;
        
        /**
         * 是否迟到
         */
        private Boolean isLate;
        
        /**
         * 迟到分钟数
         */
        private Integer lateMinutes;
        
        /**
         * 请假类型（如果是请假状态）
         */
        private String leaveType;
        
        /**
         * 请假原因（如果是请假状态）
         */
        private String leaveReason;
    }
    
    /**
     * 考勤状态枚举
     */
    public enum AttendanceStatus {
        PRESENT("正常出勤"),
        LATE("迟到"),
        LEAVE("请假"),
        ABSENT("缺勤");
        
        private final String description;
        
        AttendanceStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
