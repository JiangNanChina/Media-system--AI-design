package com.example.photography.dto.response;

import com.example.photography.model.entity.CheckinRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 打卡记录详情响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "打卡记录详情信息")
public class CheckinRecordDetailResponse {
    
    @Schema(description = "记录ID", example = "1")
    private Long id;
    
    @Schema(description = "用户ID", example = "1")
    private Long userId;
    
    @Schema(description = "用户姓名", example = "张三")
    private String userName;
    
    @Schema(description = "用户部门", example = "技术部")
    private String departmentName;
    
    @Schema(description = "配置名称", example = "图书馆晚自习")
    private String configurationName;
    
    @Schema(description = "地点名称", example = "图书馆")
    private String locationName;
    
    @Schema(description = "时段名称", example = "晚自习")
    private String sessionName;
    
    @Schema(description = "签到时间")
    private LocalDateTime checkinTime;
    
    @Schema(description = "签退时间")
    private LocalDateTime checkoutTime;
    
    @Schema(description = "状态", example = "NORMAL")
    private CheckinRecord.CheckinStatus status;
    
    @Schema(description = "是否迟到", example = "false")
    private Boolean isLate;
    
    @Schema(description = "迟到分钟数", example = "0")
    private Integer lateMinutes;
    
    @Schema(description = "持续时长（分钟）", example = "120")
    private Integer durationMinutes;
    
    @Schema(description = "签到地址", example = "北京市朝阳区...")
    private String checkinAddress;
    
    @Schema(description = "备注", example = "正常签到")
    private String remark;
    
    @Schema(description = "审核状态", example = "PENDING")
    private CheckinRecord.AuditStatus auditStatus;
    
    @Schema(description = "审核人姓名", example = "管理员")
    private String auditedByName;
    
    @Schema(description = "审核时间")
    private LocalDateTime auditTime;
    
    @Schema(description = "审核备注", example = "审核通过")
    private String auditNotes;
    
    @Schema(description = "签到方式", example = "GPS")
    private String checkinMethod;
    
    @Schema(description = "签到照片URL")
    private String checkinPhoto;
    
    @Schema(description = "用户备注")
    private String notes;
    
    /**
     * 从实体转换为DTO
     */
    public static CheckinRecordDetailResponse fromEntity(CheckinRecord record) {
        CheckinRecordDetailResponse response = new CheckinRecordDetailResponse();
        
        response.setId(record.getId());
        response.setUserId(record.getUser() != null ? record.getUser().getId() : null);
        response.setUserName(record.getUser() != null ? record.getUser().getRealName() : null);
        response.setDepartmentName(record.getUser() != null && record.getUser().getDepartment() != null ? 
            record.getUser().getDepartment().getName() : null);
        
        response.setConfigurationName(record.getConfiguration() != null ? record.getConfiguration().getName() : null);
        response.setLocationName(record.getConfiguration() != null ? record.getConfiguration().getLocationName() : null);
        response.setSessionName(record.getConfiguration() != null ? record.getConfiguration().getSessionName() : null);
        
        response.setCheckinTime(record.getCheckinTime());
        response.setCheckoutTime(record.getCheckoutTime());
        response.setStatus(record.getStatus());
        response.setIsLate(record.getIsLate());
        response.setLateMinutes(record.getLateMinutes());
        response.setDurationMinutes(record.getDurationMinutes());
        response.setCheckinAddress(record.getCheckinAddress());
        // response.setRemark(record.getRemark()); // CheckinRecord实体暂无remark字段
        
        // 审核相关字段
        response.setAuditStatus(record.getAuditStatus());
        response.setAuditedByName(record.getAuditedBy() != null ? record.getAuditedBy().getRealName() : null);
        response.setAuditTime(record.getAuditTime());
        response.setAuditNotes(record.getAuditNotes());
        
        // 其他字段
        response.setCheckinMethod(record.getCheckinMethod());
        response.setCheckinPhoto(record.getCheckinPhoto());
        response.setNotes(record.getNotes());
        
        return response;
    }
}
