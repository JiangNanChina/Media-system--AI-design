package com.example.photography.service.impl;

import com.example.photography.dto.request.CollegeCreateRequest;
import com.example.photography.dto.response.CollegeResponse;
import com.example.photography.model.entity.College;
import com.example.photography.repository.CollegeRepository;
import com.example.photography.service.CollegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学院服务实现。
 */
@Service
@Transactional
public class CollegeServiceImpl implements CollegeService {

    @Autowired
    private CollegeRepository collegeRepository;

    @Override
    public CollegeResponse createCollege(CollegeCreateRequest request) {
        String name = normalizeName(request.getName());
        if (collegeRepository.existsByNameAndDeletedFalse(name)) {
            throw new RuntimeException("学院名称已存在");
        }

        College college = new College(name);
        college.setDescription(normalizeOptional(request.getDescription()));
        return convertToResponse(collegeRepository.save(college));
    }

    @Override
    public CollegeResponse updateCollege(Long id, CollegeCreateRequest request) {
        College college = findById(id);
        String name = normalizeName(request.getName());
        if (!college.getName().equals(name) && collegeRepository.existsByNameAndDeletedFalse(name)) {
            throw new RuntimeException("学院名称已存在");
        }

        college.setName(name);
        college.setDescription(normalizeOptional(request.getDescription()));
        return convertToResponse(collegeRepository.save(college));
    }

    @Override
    public void deleteCollege(Long id) {
        College college = findById(id);
        collegeRepository.delete(college);
    }

    @Override
    @Transactional(readOnly = true)
    public College findById(Long id) {
        return collegeRepository.findById(id)
                .filter(college -> !college.getDeleted())
                .orElseThrow(() -> new RuntimeException("学院不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollegeResponse> findAllCollegeResponses() {
        return collegeRepository.findByDeletedFalseOrderByNameAsc().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollegeResponse> findAllColleges(Pageable pageable) {
        return collegeRepository.findByDeletedFalse(pageable).map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollegeResponse> searchColleges(String keyword, Pageable pageable) {
        return collegeRepository.findByNameContainingIgnoreCaseAndDeletedFalse(keyword, pageable)
                .map(this::convertToResponse);
    }

    private CollegeResponse convertToResponse(College college) {
        return new CollegeResponse(
                college.getId(),
                college.getName(),
                college.getDescription(),
                college.getCreatedAt(),
                college.getUpdatedAt()
        );
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
