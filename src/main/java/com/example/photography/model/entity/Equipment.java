package com.example.photography.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 设备实体
 */
@Entity
@Table(name = "equipment")
public class Equipment extends BaseEntity {
    
    @NotBlank(message = "设备名称不能为空")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    

    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = true)
    private EquipmentCategory category;
    
    // 兼容性字段：保留原有的字符串分类字段，用于过渡期间
    @Column(name = "category_name", length = 50)
    private String categoryName;
    
    // 旧数据库字段兼容性映射（临时解决方案）
    @Column(name = "category", length = 50)
    private String oldCategory;
    
    @NotBlank(message = "设备编号不能为空")
    @Column(name = "serial_number", nullable = false, unique = true, length = 50)
    private String serialNumber;
    
    @Column(name = "description", length = 1000)
    private String description;
    

    
    @Column(name = "image_url", length = 500)
    private String imageUrl; // 主图片URL
    
    @Column(name = "image_urls", length = 2000)
    private String imageUrls; // JSON格式存储多个图片URL
    
    @NotNull(message = "库存数量不能为空")
    @PositiveOrZero(message = "库存数量不能为负数")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;
    
    @NotNull(message = "可用数量不能为空")
    @PositiveOrZero(message = "可用数量不能为负数")
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
    
    @PositiveOrZero(message = "损坏数量不能为负数")
    @Column(name = "damaged_quantity", nullable = false)
    private Integer damagedQuantity = 0; // 损坏数量，默认为0

    
    @Column(name = "status", length = 20)
    private String status = "正常"; // 设备状态：正常、维修中、报废等
    
    @Column(name = "specifications", length = 2000)
    private String specifications; // 设备规格参数
    
    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore  // 防止JSON序列化时的循环引用
    private List<BorrowRecord> borrowRecords;
    
    // Constructors
    public Equipment() {}
    
    public Equipment(String name, EquipmentCategory category, String serialNumber) {
        this.name = name;
        this.category = category;
        this.categoryName = category != null ? category.getName() : null;
        this.serialNumber = serialNumber;
    }
    
    public Equipment(String name, String categoryName, String serialNumber) {
        this.name = name;
        this.categoryName = categoryName;
        this.serialNumber = serialNumber;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    

    
    public EquipmentCategory getCategory() {
        return category;
    }
    
    public void setCategory(EquipmentCategory category) {
        this.category = category;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getOldCategory() {
        return oldCategory;
    }
    
    public void setOldCategory(String oldCategory) {
        this.oldCategory = oldCategory;
    }
    
    // 便利方法：获取分类名称（优先从关联对象获取）
    public String getCategoryDisplayName() {
        if (category != null) {
            return category.getName();
        }
        return categoryName;
    }
    
    // JSON序列化时提供分类显示名称
    @JsonProperty("categoryDisplayName")
    public String getJsonCategoryDisplayName() {
        return getCategoryDisplayName();
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }
    
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    

    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getImageUrls() {
        return imageUrls;
    }
    
    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
    
    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
    
    public Integer getDamagedQuantity() {
        return damagedQuantity;
    }
    
    public void setDamagedQuantity(Integer damagedQuantity) {
        this.damagedQuantity = damagedQuantity;
    }

    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getSpecifications() {
        return specifications;
    }
    
    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }
    
    public List<BorrowRecord> getBorrowRecords() {
        return borrowRecords;
    }
    
    public void setBorrowRecords(List<BorrowRecord> borrowRecords) {
        this.borrowRecords = borrowRecords;
    }
}
