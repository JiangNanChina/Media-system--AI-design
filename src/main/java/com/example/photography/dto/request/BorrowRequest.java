package com.example.photography.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * 借用申请请求DTO
 */
public class BorrowRequest {
    
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;
    
    @NotNull(message = "借用数量不能为空")
    @Positive(message = "借用数量必须为正数")
    private Integer quantity;
    
    @NotNull(message = "预计归还时间不能为空")
    private LocalDateTime expectedReturnTime;
    
    private String borrowReason;
    
    // Constructors
    public BorrowRequest() {}
    
    public BorrowRequest(Long equipmentId, Integer quantity, LocalDateTime expectedReturnTime, String borrowReason) {
        this.equipmentId = equipmentId;
        this.quantity = quantity;
        this.expectedReturnTime = expectedReturnTime;
        this.borrowReason = borrowReason;
    }
    
    // Getters and Setters
    public Long getEquipmentId() {
        return equipmentId;
    }
    
    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public LocalDateTime getExpectedReturnTime() {
        return expectedReturnTime;
    }
    
    public void setExpectedReturnTime(LocalDateTime expectedReturnTime) {
        this.expectedReturnTime = expectedReturnTime;
    }
    
    public String getBorrowReason() {
        return borrowReason;
    }
    
    public void setBorrowReason(String borrowReason) {
        this.borrowReason = borrowReason;
    }
}
