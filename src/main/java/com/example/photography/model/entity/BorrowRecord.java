package com.example.photography.model.entity;

import com.example.photography.model.enums.BorrowStatus;
import com.example.photography.model.enums.BorrowerType;
import com.example.photography.model.enums.ExternalBorrowerType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * 借还记录实体
 */
@Entity
@Table(name = "borrow_records")
public class BorrowRecord extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "borrower_type", nullable = false, length = 20)
    private BorrowerType borrowerType = BorrowerType.INTERNAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "external_borrower_type", length = 20)
    private ExternalBorrowerType externalBorrowerType;

    @Column(name = "external_organization", length = 150)
    private String externalOrganization;

    @Column(name = "external_contact_name", length = 80)
    private String externalContactName;

    @Column(name = "external_phone", length = 20)
    private String externalPhone;

    @Column(name = "external_email", length = 120)
    private String externalEmail;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "借用用户不能为空")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "borrowRecords", "studyCheckins", "dutyRecords", "leaveRequests"})
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    @NotNull(message = "借用设备不能为空")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "borrowRecords"})
    private Equipment equipment;
    
    @NotNull(message = "借用数量不能为空")
    @Positive(message = "借用数量必须为正数")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @NotNull(message = "预计归还时间不能为空")
    @Column(name = "expected_return_time", nullable = false)
    private LocalDateTime expectedReturnTime;
    
    @Column(name = "actual_return_time")
    private LocalDateTime actualReturnTime;
    
    @Enumerated(EnumType.STRING)
    @NotNull(message = "借用状态不能为空")
    @Column(name = "status", nullable = false)
    private BorrowStatus status = BorrowStatus.PENDING;
    
    @Column(name = "borrow_reason", length = 500)
    private String borrowReason;
    
    @Column(name = "approval_notes", length = 500)
    private String approvalNotes; // 审核备注
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "borrowRecords", "studyCheckins", "dutyRecords", "leaveRequests"})
    private User approvedBy; // 审核人
    
    @Column(name = "approval_time")
    private LocalDateTime approvalTime;
    
    @Column(name = "return_notes", length = 500)
    private String returnNotes; // 归还备注
    
    @Column(name = "damage_description", length = 500)
    private String damageDescription; // 损坏描述
    
    @Column(name = "return_images", length = 2000)
    private String returnImages; // 归还图片JSON字符串
    
    // Constructors
    public BorrowRecord() {}
    
    public BorrowRecord(User user, Equipment equipment, Integer quantity, LocalDateTime expectedReturnTime) {
        this.user = user;
        this.equipment = equipment;
        this.quantity = quantity;
        this.expectedReturnTime = expectedReturnTime;
    }
    
    // Getters and Setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Equipment getEquipment() {
        return equipment;
    }
    
    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
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
    
    public LocalDateTime getActualReturnTime() {
        return actualReturnTime;
    }
    
    public void setActualReturnTime(LocalDateTime actualReturnTime) {
        this.actualReturnTime = actualReturnTime;
    }
    
    public BorrowStatus getStatus() {
        return status;
    }
    
    public void setStatus(BorrowStatus status) {
        this.status = status;
    }
    
    public String getBorrowReason() {
        return borrowReason;
    }
    
    public void setBorrowReason(String borrowReason) {
        this.borrowReason = borrowReason;
    }
    
    public String getApprovalNotes() {
        return approvalNotes;
    }
    
    public void setApprovalNotes(String approvalNotes) {
        this.approvalNotes = approvalNotes;
    }
    
    public User getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public LocalDateTime getApprovalTime() {
        return approvalTime;
    }
    
    public void setApprovalTime(LocalDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }
    
    public String getReturnNotes() {
        return returnNotes;
    }
    
    public void setReturnNotes(String returnNotes) {
        this.returnNotes = returnNotes;
    }
    
    public String getDamageDescription() {
        return damageDescription;
    }
    
    public void setDamageDescription(String damageDescription) {
        this.damageDescription = damageDescription;
    }
    
    public String getReturnImages() {
        return returnImages;
    }
    
    public void setReturnImages(String returnImages) {
        this.returnImages = returnImages;
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
