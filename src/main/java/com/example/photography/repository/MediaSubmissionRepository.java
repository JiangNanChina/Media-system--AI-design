package com.example.photography.repository;

import com.example.photography.model.entity.MediaSubmission;
import com.example.photography.model.enums.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaSubmissionRepository extends JpaRepository<MediaSubmission, Long> {
    Page<MediaSubmission> findByDeletedFalse(Pageable pageable);
    Page<MediaSubmission> findByStatusAndDeletedFalse(SubmissionStatus status, Pageable pageable);
    Optional<MediaSubmission> findByIdAndDeletedFalse(Long id);
    boolean existsBySubmissionNumber(String submissionNumber);
}
