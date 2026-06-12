package com.example.photography.service;

import com.example.photography.dto.request.EquipmentCreateRequest;
import com.example.photography.model.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 设备服务接口
 */
public interface EquipmentService {
    
    /**
     * 创建设备
     */
    Equipment createEquipment(EquipmentCreateRequest request);
    
    /**
     * 更新设备信息
     */
    Equipment updateEquipment(Long id, EquipmentCreateRequest request);
    
    /**
     * 删除设备
     */
    void deleteEquipment(Long id);
    
    /**
     * 根据ID查找设备
     */
    Equipment findById(Long id);
    
    /**
     * 分页查询设备
     */
    Page<Equipment> findEquipments(Pageable pageable);
    
    /**
     * 搜索设备
     */
    Page<Equipment> searchEquipments(String keyword, Pageable pageable);
    
    /**
     * 根据分类查找设备
     */
    Page<Equipment> findByCategory(String category, Pageable pageable);
    
    /**
     * 根据分类和关键字搜索设备
     */
    Page<Equipment> searchEquipmentsByCategory(String category, String keyword, Pageable pageable);
    
    /**
     * 获取所有分类
     */
    List<String> getAllCategories();
    
    /**
     * 检查设备编号是否存在
     */
    boolean existsBySerialNumber(String serialNumber);
    
    /**
     * 上传设备图片
     */
    String uploadEquipmentImage(Long equipmentId, MultipartFile file);
    
    String uploadEquipmentImages(Long equipmentId, MultipartFile[] files);
    
    /**
     * 更新设备库存
     */
    void updateStock(Long equipmentId, Integer stockQuantity, Integer availableQuantity);
    
    /**
     * 获取可用设备列表
     */
    List<Equipment> getAvailableEquipments();
    
    /**
     * 减少可用库存（借用时调用）
     */
    void decreaseAvailableStock(Long equipmentId, Integer quantity);
    
    /**
     * 增加可用库存（归还时调用）
     */
    void increaseAvailableStock(Long equipmentId, Integer quantity);
    
    /**
     * 增加损坏数量（损坏归还时调用）
     */
    void increaseDamagedQuantity(Long equipmentId, Integer quantity);
    
    /**
     * 获取设备统计信息
     */
    EquipmentStatistics getEquipmentStatistics();
    
    /**
     * 获取可用设备数量
     */
    long getAvailableEquipmentCount();
    
    /**
     * 获取所有设备（分页）
     */
    Page<Equipment> getAllEquipments(Pageable pageable);
    
    /**
     * 物理删除设备（永久删除）
     */
    void physicalDeleteEquipment(Long id);
    
    /**
     * 批量物理删除设备
     */
    void physicalDeleteEquipmentsByIds(List<Long> ids);
    
    /**
     * 获取已删除设备的统计信息
     */
    Map<String, Object> getDeletedEquipmentStatistics();
    
    /**
     * 分页查询已软删除的设备
     */
    Page<Equipment> findDeletedEquipments(Pageable pageable);
    
    /**
     * 清理指定天数前的已删除设备
     */
    int cleanupDeletedEquipments(int daysOld);
    
    /**
     * 删除设备图片文件
     */
    void deleteEquipmentImage(Long equipmentId);
    
    /**
     * 清理孤立的设备图片文件
     * @return 清理的文件数量
     */
    int cleanupOrphanedImages();
    
    /**
     * 更新设备状态
     */
    void updateEquipmentStatus(Long equipmentId, String status);
    
    /**
     * 修复设备状态与库存的不一致问题
     */
    void fixEquipmentStatusConsistency(Long equipmentId);
    
    /**
     * 批量修复所有设备的状态一致性问题
     */
    int fixAllEquipmentStatusConsistency();
    
    /**
     * 设备统计信息类
     */
    class EquipmentStatistics {
        private long totalEquipments;
        private long totalStock;
        private long availableStock;
        private long borrowedStock;
        private long normalEquipments;
        private long maintenanceEquipments;
        private long scrapedEquipments;
        private long categoryCount;
        
        // Constructors, getters and setters
        public EquipmentStatistics() {}
        
        public EquipmentStatistics(long totalEquipments, long totalStock, long availableStock, 
                                 long borrowedStock, long normalEquipments, long maintenanceEquipments, 
                                 long scrapedEquipments, long categoryCount) {
            this.totalEquipments = totalEquipments;
            this.totalStock = totalStock;
            this.availableStock = availableStock;
            this.borrowedStock = borrowedStock;
            this.normalEquipments = normalEquipments;
            this.maintenanceEquipments = maintenanceEquipments;
            this.scrapedEquipments = scrapedEquipments;
            this.categoryCount = categoryCount;
        }
        
        public long getTotalEquipments() { return totalEquipments; }
        public void setTotalEquipments(long totalEquipments) { this.totalEquipments = totalEquipments; }
        
        public long getTotalStock() { return totalStock; }
        public void setTotalStock(long totalStock) { this.totalStock = totalStock; }
        
        public long getAvailableStock() { return availableStock; }
        public void setAvailableStock(long availableStock) { this.availableStock = availableStock; }
        
        public long getBorrowedStock() { return borrowedStock; }
        public void setBorrowedStock(long borrowedStock) { this.borrowedStock = borrowedStock; }
        
        public long getNormalEquipments() { return normalEquipments; }
        public void setNormalEquipments(long normalEquipments) { this.normalEquipments = normalEquipments; }
        
        public long getMaintenanceEquipments() { return maintenanceEquipments; }
        public void setMaintenanceEquipments(long maintenanceEquipments) { this.maintenanceEquipments = maintenanceEquipments; }
        
        public long getScrapedEquipments() { return scrapedEquipments; }
        public void setScrapedEquipments(long scrapedEquipments) { this.scrapedEquipments = scrapedEquipments; }
        
        public long getCategoryCount() { return categoryCount; }
        public void setCategoryCount(long categoryCount) { this.categoryCount = categoryCount; }
        
        @Override
        public String toString() {
            return "EquipmentStatistics{" +
                "totalEquipments=" + totalEquipments +
                ", totalStock=" + totalStock +
                ", availableStock=" + availableStock +
                ", borrowedStock=" + borrowedStock +
                ", normalEquipments=" + normalEquipments +
                ", maintenanceEquipments=" + maintenanceEquipments +
                ", scrapedEquipments=" + scrapedEquipments +
                ", categoryCount=" + categoryCount +
                '}';
        }
    }
}
