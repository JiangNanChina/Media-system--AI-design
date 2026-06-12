package com.example.photography.repository;

import com.example.photography.model.entity.Announcement;
import com.example.photography.model.enums.AnnouncementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 公告Repository
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    
    /**
     * 根据ID查找公告（预加载创建者信息）
     */
    @Query("SELECT a FROM Announcement a LEFT JOIN FETCH a.author WHERE a.id = :id AND a.deleted = false")
    Optional<Announcement> findByIdWithAuthor(@Param("id") Long id);
    
    /**
     * 查找已发布的公告，按优先级和创建时间排序
     */
    @Query("SELECT a FROM Announcement a WHERE a.deleted = false AND a.published = true " +
           "ORDER BY a.priority DESC, a.createdAt DESC")
    List<Announcement> findPublishedAnnouncements();
    
    /**
     * 分页查找已发布的公告
     */
    @Query("SELECT a FROM Announcement a WHERE a.published = true AND a.deleted = false ORDER BY a.priority DESC, a.createdAt DESC")
    Page<Announcement> findPublishedAnnouncementsPaged(Pageable pageable);
    
    /**
     * 分页查找已发布的公告（预加载创建者信息）
     */
    @Query("SELECT a FROM Announcement a LEFT JOIN FETCH a.author WHERE a.published = true AND a.deleted = false ORDER BY a.priority DESC, a.createdAt DESC")
    Page<Announcement> findPublishedAnnouncementsWithAuthorPaged(Pageable pageable);
    
    /**
     * 查找所有公告（管理员用）
     */
    Page<Announcement> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 查找所有公告（预加载创建者信息）
     */
    @Query("SELECT a FROM Announcement a LEFT JOIN FETCH a.author WHERE a.deleted = false ORDER BY a.createdAt DESC")
    Page<Announcement> findByDeletedFalseWithAuthorOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 根据创建者查找公告
     */
    Page<Announcement> findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(Long authorId, Pageable pageable);
    
    /**
     * 根据标题搜索公告
     */
    @Query("SELECT a FROM Announcement a WHERE a.deleted = false AND " +
           "(a.title LIKE %:keyword% OR a.content LIKE %:keyword%) " +
           "ORDER BY a.createdAt DESC")
    Page<Announcement> searchAnnouncements(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 增加查看次数
     */
    @Modifying
    @Query("UPDATE Announcement a SET a.viewCount = COALESCE(a.viewCount, 0) + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") Long id);
    
    /**
     * 修复现有数据的viewCount为NULL的问题
     */
    @Modifying
    @Query("UPDATE Announcement a SET a.viewCount = 0 WHERE a.viewCount IS NULL")
    void fixNullViewCounts();
    
    /**
     * 查找最新的公告（首页展示用）
     */
    @Query("SELECT a FROM Announcement a WHERE a.deleted = false AND a.published = true " +
           "ORDER BY a.priority DESC, a.createdAt DESC")
    List<Announcement> findLatestAnnouncements(Pageable pageable);
    
    /**
     * 查找登录弹窗公告（排除归档的公告）
     */
    @Query("SELECT a FROM Announcement a WHERE a.deleted = false AND a.published = true " +
           "AND (a.archived = false OR a.archived IS NULL) " +
           "ORDER BY a.priority DESC, a.createdAt DESC")
    List<Announcement> findLoginPopupAnnouncements(Pageable pageable);
    
    /**
     * 分页查找登录弹窗公告（排除归档的公告）
     */
    @Query("SELECT a FROM Announcement a LEFT JOIN FETCH a.author WHERE a.deleted = false AND a.published = true " +
           "AND (a.archived = false OR a.archived IS NULL) " +
           "ORDER BY a.priority DESC, a.createdAt DESC")
    Page<Announcement> findByPublishedTrueAndDeletedFalseAndArchivedFalseOrderByPriorityDescCreatedAtDesc(Pageable pageable);
    
    /**
     * 统计公告数量
     */
    long countByDeletedFalse();
    
    /**
     * 统计已发布公告数量
     */
    long countByPublishedTrueAndDeletedFalse();
    
    /**
     * 根据多条件搜索公告（预加载创建者信息）
     */
    @Query("SELECT a FROM Announcement a LEFT JOIN FETCH a.author " +
           "WHERE a.deleted = false " +
           "AND (:keyword IS NULL OR :keyword = '' OR a.title LIKE %:keyword% OR a.content LIKE %:keyword%) " +
           "AND (:typeEnum IS NULL OR a.type = :typeEnum) " +
           "AND (:published IS NULL OR a.published = :published) " +
           "ORDER BY a.createdAt DESC")
    Page<Announcement> searchAnnouncementsWithAuthor(
            @Param("keyword") String keyword,
            @Param("typeEnum") AnnouncementType typeEnum,
            @Param("published") Boolean published,
            Pageable pageable);
}
