package com.example.photography.service;

import com.example.photography.model.entity.*;
import com.example.photography.dto.request.CheckinRequest;
import com.example.photography.dto.request.CheckoutRequest;
import com.example.photography.dto.response.CheckinResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 打卡服务接口
 */
public interface CheckinService {
    
    /**
     * 用户签到
     */
    CheckinResponse checkin(CheckinRequest request, Long userId);
    
    /**
     * 用户签退
     */
    CheckinResponse checkout(CheckoutRequest request, Long userId);
    
    /**
     * 获取用户今日打卡状态
     */
    Map<String, Object> getTodayCheckinStatus(Long userId);
    
    /**
     * 获取用户打卡记录
     */
    Page<CheckinRecord> getUserCheckinRecords(Long userId, Pageable pageable);
    
    /**
     * 获取用户最近的打卡记录（含完整关联数据）
     */
    List<CheckinRecord> getRecentCheckinRecords(Long userId, int size);
    
    /**
     * 获取用户指定日期范围的打卡记录
     */
    List<CheckinRecord> getUserCheckinRecords(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取所有打卡记录（管理员）
     */
    Page<CheckinRecord> getAllCheckinRecords(String keyword, CheckinRecord.CheckinStatus status, 
                                           LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * 获取打卡记录详情
     */
    CheckinRecord getCheckinRecordById(Long id);
    
    /**
     * 管理员补签
     */
    CheckinRecord makeupCheckin(Long recordId, String notes, Long adminId);
    
    /**
     * 删除打卡记录
     */
    void deleteCheckinRecord(Long id);
    
    /**
     * 验证打卡配置是否有效（包含用户权限验证）
     */
    boolean validateConfiguration(Long configurationId, Double latitude, Double longitude, Long userId, String checkinMethod);
    
    /**
     * 上传打卡照片
     */
    String uploadCheckinPhoto(MultipartFile file, Long recordId);
    
    /**
     * 获取打卡统计数据
     */
    Map<String, Object> getCheckinStatistics(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取全局打卡统计数据（管理员）
     */
    Map<String, Object> getGlobalCheckinStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * 导出打卡记录
     */
    byte[] exportCheckinRecords(String keyword, CheckinRecord.CheckinStatus status, 
                               LocalDate startDate, LocalDate endDate);
    
    /**
     * 自动处理过期的打卡记录
     */
    void processExpiredRecords();
    
    /**
     * 获取用户打卡排行榜
     */
    List<Map<String, Object>> getCheckinRanking(LocalDate startDate, LocalDate endDate, int limit);
    
    /**
     * 删除打卡记录
     * @param recordId 记录ID
     * @param userId 用户ID（用于权限验证）
     * @param isAdmin 是否为管理员
     * @return 是否删除成功
     */
    boolean deleteCheckinRecord(Long recordId, Long userId, boolean isAdmin);
    
    /**
     * 获取用户有权限的打卡配置列表
     * @param userId 用户ID
     * @return 用户可以打卡的配置列表
     */
    List<CheckinConfiguration> getUserAuthorizedConfigurations(Long userId);
    
    /**
     * 管理员审核签到记录 - 通过
     * @param recordId 记录ID
     * @param adminId 审核管理员ID
     * @param auditNotes 审核备注
     * @return 审核后的记录
     */
    CheckinRecord approveCheckin(Long recordId, Long adminId, String auditNotes);
    
    /**
     * 管理员审核签到记录 - 拒绝（标记为缺勤）
     * @param recordId 记录ID
     * @param adminId 审核管理员ID
     * @param auditNotes 审核备注
     * @return 审核后的记录
     */
    CheckinRecord rejectCheckin(Long recordId, Long adminId, String auditNotes);
    
    /**
     * 获取待审核的签到记录列表
     * @param pageable 分页参数
     * @return 待审核记录分页列表
     */
    Page<CheckinRecord> getPendingAuditRecords(Pageable pageable);
    
    /**
     * 根据审核状态获取签到记录列表
     * @param auditStatus 审核状态
     * @param pageable 分页参数
     * @return 指定审核状态的记录分页列表
     */
    Page<CheckinRecord> getAuditRecordsByStatus(CheckinRecord.AuditStatus auditStatus, Pageable pageable);
    
    /**
     * 获取待审核记录数量
     * @return 待审核记录总数
     */
    long getPendingAuditCount();
    
    /**
     * 物理删除签到记录（永久删除，释放空间）
     * @param recordId 记录ID
     */
    void deleteCheckinRecordPhysically(Long recordId);
}