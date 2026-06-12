package com.example.photography.dto.request;

import java.util.List;

/**
 * 归还请求DTO
 */
public class ReturnRequest {
    
    private String condition; // 设备状态：normal/damaged
    
    private String returnNotes; // 归还备注
    
    private String damageDescription; // 损坏描述
    
    private List<String> returnImages; // 归还图片URL列表
    
    // Constructors
    public ReturnRequest() {}
    
    public ReturnRequest(String condition, String returnNotes, String damageDescription, List<String> returnImages) {
        this.condition = condition;
        this.returnNotes = returnNotes;
        this.damageDescription = damageDescription;
        this.returnImages = returnImages;
    }
    
    // Getters and Setters
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
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
    
    public List<String> getReturnImages() {
        return returnImages;
    }
    
    public void setReturnImages(List<String> returnImages) {
        this.returnImages = returnImages;
    }
}
