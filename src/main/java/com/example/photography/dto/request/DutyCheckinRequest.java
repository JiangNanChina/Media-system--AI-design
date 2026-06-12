package com.example.photography.dto.request;

/**
 * 执勤打卡请求DTO
 */
public class DutyCheckinRequest {
    
    private String notes; // 备注
    
    // Constructors
    public DutyCheckinRequest() {}
    
    public DutyCheckinRequest(String notes) {
        this.notes = notes;
    }
    
    // Getters and Setters
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
