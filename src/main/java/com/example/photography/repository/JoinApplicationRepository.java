package com.example.photography.repository;

import com.example.photography.model.entity.JoinApplication;
import com.example.photography.model.enums.JoinApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JoinApplicationRepository extends JpaRepository<JoinApplication, Long> {
    boolean existsByApplicationNumber(String applicationNumber);

    Page<JoinApplication> findByDeletedFalse(Pageable pageable);

    Page<JoinApplication> findByStatusAndDeletedFalse(JoinApplicationStatus status, Pageable pageable);

    Optional<JoinApplication> findByIdAndDeletedFalse(Long id);
}
