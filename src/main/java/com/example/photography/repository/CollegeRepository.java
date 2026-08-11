package com.example.photography.repository;

import com.example.photography.model.entity.College;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学院 Repository。
 */
@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {

    Optional<College> findByNameAndDeletedFalse(String name);

    List<College> findByDeletedFalseOrderByNameAsc();

    Page<College> findByDeletedFalse(Pageable pageable);

    Page<College> findByNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);

    boolean existsByNameAndDeletedFalse(String name);
}
