package com.example.photography.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;



/**
 * 设备创建请求DTO
 */
public class EquipmentCreateRequest {
    
    @NotBlank(message = "设备名称不能为空")
    private String name;
    

    
    @NotBlank(message = "设备分类不能为空")
    private String category;
    
    @NotBlank(message = "设备编号不能为空")
    private String serialNumber;
    
    private String description;
    

    
    @NotNull(message = "库存数量不能为空")
    @PositiveOrZero(message = "库存数量不能为负数")
    private Integer stockQuantity;
    
    @NotNull(message = "可用数量不能为空")
    @PositiveOrZero(message = "可用数量不能为负数")
    private Integer availableQuantity;
    
    @PositiveOrZero(message = "损坏数量不能为负数")
    private Integer damagedQuantity = 0;

    
    private String status = "正常";
    
    private String specifications;
    
    // Constructors
    public EquipmentCreateRequest() {}
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    

    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
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
}
