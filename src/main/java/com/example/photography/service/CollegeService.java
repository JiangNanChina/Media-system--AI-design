package com.example.photography.service;

import com.example.photography.dto.request.CollegeCreateRequest;
import com.example.photography.dto.response.CollegeResponse;
import com.example.photography.model.entity.College;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 学院服务接口。
 */
public interface CollegeService {

    CollegeResponse createCollege(CollegeCreateRequest request);

    CollegeResponse updateCollege(Long id, CollegeCreateRequest request);

    void deleteCollege(Long id);

    College findById(Long id);

    List<CollegeResponse> findAllCollegeResponses();

    Page<CollegeResponse> findAllColleges(Pageable pageable);

    Page<CollegeResponse> searchColleges(String keyword, Pageable pageable);
}
