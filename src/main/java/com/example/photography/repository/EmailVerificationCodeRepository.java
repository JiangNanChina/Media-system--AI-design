package com.example.photography.repository;

import com.example.photography.model.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 邮箱验证码Repository
 */
@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {
    Optional<EmailVerificationCode> findTopByEmailAndPurposeAndUsedAtIsNullAndDeletedFalseOrderByCreatedAtDesc(String email, String purpose);

    List<EmailVerificationCode> findByEmailAndPurposeAndUsedAtIsNullAndDeletedFalse(String email, String purpose);

    @Modifying
    @Query("DELETE FROM EmailVerificationCode c " +
            "WHERE c.createdAt < :cutoff " +
            "AND (c.expiresAt < :now OR c.usedAt IS NOT NULL OR c.deleted = true)")
    int deleteExpiredOrInactiveBefore(@Param("cutoff") LocalDateTime cutoff, @Param("now") LocalDateTime now);
}
