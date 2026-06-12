package com.example.photography.dto.response;

import com.example.photography.model.entity.CheckinRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户考勤状态响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户考勤状态信息")
public class UserAttendanceStatusResponse {
    
    @Schema(description = "用户ID", example = "1")
    private Long userId;
    
    @Schema(description = "用户姓名", example = "张三")
    private String userName;
    
    @Schema(description = "用户部门", example = "技术部")
    private String departmentName;
    
    @Schema(description = "用户头像URL")
    private String avatarUrl;
    
    @Schema(description = "考勤状态", example = "CHECKED_IN")
    private AttendanceStatus status;
    
    @Schema(description = "签到时间")
    private LocalDateTime checkinTime;
    
    @Schema(description = "签退时间")
    private LocalDateTime checkoutTime;
    
    @Schema(description = "是否迟到", example = "false")
    private Boolean isLate;
    
    @Schema(description = "迟到分钟数", example = "0")
    private Integer lateMinutes;
    
    @Schema(description = "持续时长（分钟）", example = "120")
    private Integer durationMinutes;
    
    @Schema(description = "签到状态", example = "NORMAL")
    private CheckinRecord.CheckinStatus checkinStatus;
    
    @Schema(description = "请假类型（如果请假的话）")
    private String leaveType;
    
    @Schema(description = "备注信息")
    private String remark;
    
    @Schema(description = "审核状态", example = "PENDING")
    private CheckinRecord.AuditStatus auditStatus;
    
    @Schema(description = "审核人姓名", example = "管理员")
    private String auditedByName;
    
    @Schema(description = "审核时间")
    private LocalDateTime auditTime;
    
    @Schema(description = "审核备注", example = "审核通过")
    private String auditNotes;
    
    /**
     * 考勤状态枚举
     */
    public enum AttendanceStatus {
        CHECKED_IN("已签到"),
        ABSENT("缺勤"),
        ON_LEAVE("请假");
        
        private final String description;
        
        AttendanceStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
