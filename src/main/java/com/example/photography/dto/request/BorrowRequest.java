package com.example.photography.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import com.example.photography.model.enums.BorrowerType;
import com.example.photography.model.enums.ExternalBorrowerType;

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

    private BorrowerType borrowerType = BorrowerType.INTERNAL;
    private ExternalBorrowerType externalBorrowerType;
    private String externalOrganization;
    private String externalContactName;
    private String externalPhone;
    private String externalEmail;
    
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

    public BorrowerType getBorrowerType() { return borrowerType; }
    public void setBorrowerType(BorrowerType borrowerType) { this.borrowerType = borrowerType; }
    public ExternalBorrowerType getExternalBorrowerType() { return externalBorrowerType; }
    public void setExternalBorrowerType(ExternalBorrowerType externalBorrowerType) { this.externalBorrowerType = externalBorrowerType; }
    public String getExternalOrganization() { return externalOrganization; }
    public void setExternalOrganization(String externalOrganization) { this.externalOrganization = externalOrganization; }
    public String getExternalContactName() { return externalContactName; }
    public void setExternalContactName(String externalContactName) { this.externalContactName = externalContactName; }
    public String getExternalPhone() { return externalPhone; }
    public void setExternalPhone(String externalPhone) { this.externalPhone = externalPhone; }
    public String getExternalEmail() { return externalEmail; }
    public void setExternalEmail(String externalEmail) { this.externalEmail = externalEmail; }
}
