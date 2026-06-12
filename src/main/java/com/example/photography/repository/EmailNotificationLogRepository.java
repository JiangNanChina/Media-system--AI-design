package com.example.photography.repository;

import com.example.photography.model.entity.EmailNotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 邮件通知日志Repository
 */
@Repository
public interface EmailNotificationLogRepository extends JpaRepository<EmailNotificationLog, Long> {
    @Query("SELECT COUNT(l) > 0 FROM EmailNotificationLog l " +
           "WHERE l.notificationType = :notificationType " +
           "AND l.businessId = :businessId " +
           "AND l.recipientEmail = :recipientEmail " +
           "AND l.periodKey = :periodKey " +
           "AND l.success = true " +
           "AND l.deleted = false")
    boolean existsSuccessfulLog(@Param("notificationType") String notificationType,
                                @Param("businessId") Long businessId,
                                @Param("recipientEmail") String recipientEmail,
                                @Param("periodKey") String periodKey);

    @Query("SELECT l FROM EmailNotificationLog l " +
           "WHERE l.deleted = false " +
           "AND (:notificationType IS NULL OR :notificationType = '' OR l.notificationType = :notificationType) " +
           "AND (:success IS NULL OR l.success = :success) " +
           "ORDER BY l.sentAt DESC, l.id DESC")
    Page<EmailNotificationLog> searchLogs(@Param("notificationType") String notificationType,
                                          @Param("success") Boolean success,
                                          Pageable pageable);
}
