package com.example.photography.dto.response;

import com.example.photography.model.enums.BorrowStatus;
import com.example.photography.model.enums.BorrowerType;
import com.example.photography.model.enums.ExternalBorrowerType;

import java.time.LocalDateTime;

/**
 * 借用记录响应DTO
 */
public class BorrowRecordResponse {
    
    private Long id;
    private UserInfo user;
    private EquipmentInfo equipment;
    private Integer quantity;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualReturnTime;
    private BorrowStatus status;
    private String purpose;  // 对应前端的借用目的
    private String borrowReason;
    private String approvalNotes;
    private String approvedByName;
    private LocalDateTime approvalTime;
    private String returnNotes;
    private String damageDescription;
    private String returnImages; // 归还图片JSON字符串
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BorrowerType borrowerType;
    private ExternalBorrowerType externalBorrowerType;
    private String externalOrganization;
    private String externalContactName;
    private String externalPhone;
    private String externalEmail;
    
    // 嵌套类：用户信息
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String departmentName;
        
        public UserInfo() {}
        
        public UserInfo(Long id, String username, String realName, String departmentName) {
            this.id = id;
            this.username = username;
            this.realName = realName;
            this.departmentName = departmentName;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    }
    
    // 嵌套类：设备信息
    public static class EquipmentInfo {
        private Long id;
        private String name;
        private String category;
        private String serialNumber;
        private String imageUrl;
        private String specifications;
        
        public EquipmentInfo() {}
        
        public EquipmentInfo(Long id, String name, String category, String serialNumber) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.serialNumber = serialNumber;
        }
        
        public EquipmentInfo(Long id, String name, String category, String serialNumber, String imageUrl, String specifications) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.serialNumber = serialNumber;
            this.imageUrl = imageUrl;
            this.specifications = specifications;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getSpecifications() { return specifications; }
        public void setSpecifications(String specifications) { this.specifications = specifications; }
    }
    
    // Constructors
    public BorrowRecordResponse() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public UserInfo getUser() {
        return user;
    }
    
    public void setUser(UserInfo user) {
        this.user = user;
    }
    
    public EquipmentInfo getEquipment() {
        return equipment;
    }
    
    public void setEquipment(EquipmentInfo equipment) {
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
    
    public String getPurpose() {
        return purpose;
    }
    
    public void setPurpose(String purpose) {
        this.purpose = purpose;
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
    
    public String getApprovedByName() {
        return approvedByName;
    }
    
    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
