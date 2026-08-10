package com.example.photography.repository;

import com.example.photography.model.entity.LandingContentItem;
import com.example.photography.model.enums.LandingSectionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LandingContentItemRepository extends JpaRepository<LandingContentItem, Long> {
    List<LandingContentItem> findByPublishedTrueAndDeletedFalseOrderBySectionTypeAscSortOrderAsc();
    List<LandingContentItem> findBySectionTypeAndDeletedFalseOrderBySortOrderAsc(LandingSectionType sectionType);
    List<LandingContentItem> findByDeletedFalseOrderBySectionTypeAscSortOrderAsc();
}
