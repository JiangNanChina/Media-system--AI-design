package com.example.photography.dto.response;

import java.time.LocalDateTime;

/**
 * 已删除借用记录响应DTO
 * 用于数据清理功能，避免Hibernate懒加载代理序列化问题
 */
public class DeletedBorrowRecordResponse {
    private Long id;
    private String equipmentName;
    private String equipmentSerial;
    private String username;
    private String realName;
    private String status;
    private LocalDateTime updatedAt; // 删除时间

    // 默认构造函数
    public DeletedBorrowRecordResponse() {}

    // 全参构造函数
    public DeletedBorrowRecordResponse(Long id, String equipmentName, String equipmentSerial, 
                                      String username, String realName, String status, 
                                      LocalDateTime updatedAt) {
        this.id = id;
        this.equipmentName = equipmentName;
        this.equipmentSerial = equipmentSerial;
        this.username = username;
        this.realName = realName;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getEquipmentSerial() {
        return equipmentSerial;
    }

    public void setEquipmentSerial(String equipmentSerial) {
        this.equipmentSerial = equipmentSerial;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
