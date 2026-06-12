package com.example.photography.dto.response;

import com.example.photography.model.entity.CheckinRecord;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 打卡响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckinResponse {
    
    private Long id;
    private String message;
    private LocalDateTime time;
    private String status;
    private Boolean isLate;
    private Integer lateMinutes;
    private String locationName;
    private String sessionName;
    private Integer durationMinutes;
    private String auditStatus;  // 审核状态：NOT_REQUIRED, PENDING, APPROVED, REJECTED
    
    public static CheckinResponse fromEntity(CheckinRecord record, String message) {
        CheckinResponse response = new CheckinResponse();
        response.setId(record.getId());
        response.setMessage(message);
        response.setTime(record.getCheckinTime());
        response.setStatus(record.getStatus().getDescription());
        response.setIsLate(record.getIsLate());
        response.setLateMinutes(record.getLateMinutes());
        response.setLocationName(record.getConfiguration().getLocationName());
        response.setSessionName(record.getConfiguration().getSessionName());
        response.setDurationMinutes(record.getDurationMinutes());
        response.setAuditStatus(record.getAuditStatus() != null ? record.getAuditStatus().name() : null);
        return response;
    }
}
