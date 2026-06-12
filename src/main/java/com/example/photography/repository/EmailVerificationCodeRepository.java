package com.example.photography.repository;

import com.example.photography.model.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 邮箱验证码Repository
 */
@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {
    Optional<EmailVerificationCode> findTopByEmailAndPurposeAndUsedAtIsNullAndDeletedFalseOrderByCreatedAtDesc(String email, String purpose);

    List<EmailVerificationCode> findByEmailAndPurposeAndUsedAtIsNullAndDeletedFalse(String email, String purpose);
}
