package com.example.photography.service;

import com.example.photography.dto.request.BorrowApprovalRequest;
import com.example.photography.dto.request.BorrowRequest;
import com.example.photography.dto.request.ReturnRequest;
import com.example.photography.dto.response.BorrowRecordResponse;
import com.example.photography.dto.response.DeletedBorrowRecordResponse;
import com.example.photography.model.entity.BorrowRecord;
import com.example.photography.model.enums.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 借还管理服务接口
 */
public interface BorrowService {
    
    /**
     * 申请借用设备
     */
    BorrowRecord submitBorrowRequest(Long userId, BorrowRequest request);
    
    /**
     * 审批借用申请
     */
    BorrowRecord approveBorrowRequest(Long recordId, Long approverId, BorrowApprovalRequest request);
    
    /**
     * 上传归还设备图片
     */
    String uploadReturnImage(Long recordId, MultipartFile file);
    
    /**
     * 归还设备
     */
    BorrowRecord returnEquipment(Long recordId, ReturnRequest request);
    
    /**
     * 根据ID查找借还记录
     */
    BorrowRecord findById(Long id);
    
    /**
     * 获取借用记录详情
     */
    BorrowRecordResponse getBorrowRecordDetail(Long id);
    
    /**
     * 分页查询所有借还记录
     */
    Page<BorrowRecord> findAllRecords(Pageable pageable);
    
    /**
     * 分页查询所有借还记录（返回DTO）
     */
    Page<BorrowRecordResponse> findAllRecordsResponse(Pageable pageable);
    
    /**
     * 根据用户ID查找借还记录
     */
    Page<BorrowRecord> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID查找借还记录（返回DTO）
     */
    Page<BorrowRecordResponse> findByUserIdResponse(Long userId, Pageable pageable);
    
    /**
     * 根据设备ID查找借还记录
     */
    Page<BorrowRecord> findByEquipmentId(Long equipmentId, Pageable pageable);
    
    /**
     * 根据设备ID查找借还记录（Response）
     */
    Page<BorrowRecordResponse> findByEquipmentIdResponse(Long equipmentId, Pageable pageable);
    
    /**
     * 根据状态查找借还记录
     */
    Page<BorrowRecord> findByStatus(BorrowStatus status, Pageable pageable);
    
    /**
     * 根据状态查找借还记录（返回DTO）
     */
    Page<BorrowRecordResponse> findByStatusResponse(BorrowStatus status, Pageable pageable);
    
    /**
     * 获取待审核的借用申请
     */
    List<BorrowRecord> getPendingRequests();
    
    /**
     * 获取待审核的借用申请（Response）
     */
    List<BorrowRecordResponse> getPendingRequestsResponse();
    
    /**
     * 获取用户当前借用的设备
     */
    List<BorrowRecord> getUserCurrentBorrows(Long userId);
    
    /**
     * 获取用户当前借用的设备（Response）
     */
    List<BorrowRecordResponse> getUserCurrentBorrowsResponse(Long userId);
    
    /**
     * 导出借用记录到Excel
     */
    byte[] exportBorrowRecords(BorrowStatus status, String startDate, String endDate);
    
    /**
     * 删除借用记录（仅管理员）
     */
    void deleteBorrowRecord(Long id);
    
    /**
     * 取消借用申请（用户取消自己的申请）
     */
    void cancelBorrowRequest(Long id, String username);
    
    /**
     * 获取逾期记录
     */
    List<BorrowRecord> getOverdueRecords();
    
    /**
     * 获取逾期记录（Response）
     */
    Page<BorrowRecordResponse> getOverdueRecordsResponse(Pageable pageable);
    
    /**
     * 根据时间范围查找记录
     */
    List<BorrowRecord> findByDateRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 物理删除借用记录（永久删除）
     */
    void physicalDeleteBorrowRecord(Long id);
    
    /**
     * 批量物理删除借用记录
     */
    void physicalDeleteBorrowRecordsByIds(List<Long> ids);
    
    /**
     * 获取已删除记录的统计信息
     */
    Map<String, Object> getDeletedRecordsStatistics();
    
    /**
     * 分页查询已软删除的记录
     */
    Page<DeletedBorrowRecordResponse> findDeletedRecords(Pageable pageable);
    
    /**
     * 清理指定天数前的已删除记录
     */
    int cleanupDeletedRecords(int daysOld);
    

    
    /**
     * 获取借还统计信息
     */
    BorrowStatistics getBorrowStatistics();
    
    /**
     * 获取用户个人借用统计信息
     */
    UserBorrowStatistics getUserBorrowStatistics(Long userId);
    
    /**
     * 借还统计信息类
     */
    class BorrowStatistics {
        private long totalRecords;
        private long pendingRequests;
        private long approvedRecords;
        private long rejectedRecords;
        private long borrowedRecords;
        private long returnedRecords;
        private long overdueRecords;
        
        // Constructors, getters and setters
        public BorrowStatistics() {}
        
        public BorrowStatistics(long totalRecords, long pendingRequests, long approvedRecords,
                              long rejectedRecords, long borrowedRecords, long returnedRecords,
                              long overdueRecords) {
            this.totalRecords = totalRecords;
            this.pendingRequests = pendingRequests;
            this.approvedRecords = approvedRecords;
            this.rejectedRecords = rejectedRecords;
            this.borrowedRecords = borrowedRecords;
            this.returnedRecords = returnedRecords;
            this.overdueRecords = overdueRecords;
        }
        
        public long getTotalRecords() { return totalRecords; }
        public void setTotalRecords(long totalRecords) { this.totalRecords = totalRecords; }
        
        public long getPendingRequests() { return pendingRequests; }
        public void setPendingRequests(long pendingRequests) { this.pendingRequests = pendingRequests; }
        
        public long getApprovedRecords() { return approvedRecords; }
        public void setApprovedRecords(long approvedRecords) { this.approvedRecords = approvedRecords; }
        
        public long getRejectedRecords() { return rejectedRecords; }
        public void setRejectedRecords(long rejectedRecords) { this.rejectedRecords = rejectedRecords; }
        
        public long getBorrowedRecords() { return borrowedRecords; }
        public void setBorrowedRecords(long borrowedRecords) { this.borrowedRecords = borrowedRecords; }
        
        public long getReturnedRecords() { return returnedRecords; }
        public void setReturnedRecords(long returnedRecords) { this.returnedRecords = returnedRecords; }
        
        public long getOverdueRecords() { return overdueRecords; }
        public void setOverdueRecords(long overdueRecords) { this.overdueRecords = overdueRecords; }
    }
    
    /**
     * 用户个人借用统计信息类
     */
    class UserBorrowStatistics {
        private long totalBorrows;
        private long currentBorrows;
        private long returnedBorrows;
        private long rejectedRequests;
        
        public UserBorrowStatistics() {}
        
        public UserBorrowStatistics(long totalBorrows, long currentBorrows, long returnedBorrows, long rejectedRequests) {
            this.totalBorrows = totalBorrows;
            this.currentBorrows = currentBorrows;
            this.returnedBorrows = returnedBorrows;
            this.rejectedRequests = rejectedRequests;
        }
        
        public long getTotalBorrows() { return totalBorrows; }
        public void setTotalBorrows(long totalBorrows) { this.totalBorrows = totalBorrows; }
        
        public long getCurrentBorrows() { return currentBorrows; }
        public void setCurrentBorrows(long currentBorrows) { this.currentBorrows = currentBorrows; }
        
        public long getReturnedBorrows() { return returnedBorrows; }
        public void setReturnedBorrows(long returnedBorrows) { this.returnedBorrows = returnedBorrows; }
        
        public long getRejectedRequests() { return rejectedRequests; }
        public void setRejectedRequests(long rejectedRequests) { this.rejectedRequests = rejectedRequests; }
    }
}
