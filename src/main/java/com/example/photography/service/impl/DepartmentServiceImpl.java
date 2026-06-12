package com.example.photography.service.impl;

import com.example.photography.dto.request.DepartmentCreateRequest;
import com.example.photography.dto.response.DepartmentResponse;
import com.example.photography.model.entity.Department;
import com.example.photography.model.entity.User;
import com.example.photography.repository.DepartmentRepository;
import com.example.photography.repository.UserRepository;
import com.example.photography.service.DepartmentService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务实现类
 */
@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {
    
    private static final Logger log = LoggerFactory.getLogger(DepartmentServiceImpl.class);
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostConstruct
    public void init() {
        initializeDefaultDepartments();
    }
    
    @Override
    public Department createDepartment(DepartmentCreateRequest request) {
        // 检查部门名称是否存在
        if (departmentRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new RuntimeException("部门名称已存在");
        }
        
        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        
        return departmentRepository.save(department);
    }
    
    @Override
    public DepartmentResponse createDepartmentResponse(DepartmentCreateRequest request) {
        Department department = createDepartment(request);
        return convertToDepartmentResponse(department);
    }
    
    @Override
    public Department updateDepartment(Long id, DepartmentCreateRequest request) {
        Department department = findById(id);
        
        // 检查部门名称是否被其他部门使用
        if (!department.getName().equals(request.getName()) && 
            departmentRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new RuntimeException("部门名称已存在");
        }
        
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        
        return departmentRepository.save(department);
    }
    
    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = findById(id);
        
        // 检查是否有活跃用户关联到该部门（只检查未删除的用户）
        List<User> activeUsers = userRepository.findByDepartmentIdAndDeletedFalse(id);
        if (!activeUsers.isEmpty()) {
            throw new RuntimeException("该部门下还有 " + activeUsers.size() + " 个活跃用户，无法删除。请先转移或删除这些用户。");
        }
        
        // 物理删除部门记录
        departmentRepository.delete(department);
        
        log.info("部门已物理删除: ID={}, 名称={}", id, department.getName());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Department findById(Long id) {
        return departmentRepository.findById(id)
                .filter(dept -> !dept.getDeleted())
                .orElseThrow(() -> new RuntimeException("部门不存在"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Department> findAllDepartments() {
        return departmentRepository.findByDeletedFalse();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAllDepartmentsResponse() {
        List<Department> departments = departmentRepository.findByDeletedFalse();
        return departments.stream()
                .map(this::convertToDepartmentResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Department> findAllDepartments(Pageable pageable) {
        return departmentRepository.findByDeletedFalse(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Department> searchDepartments(String keyword, Pageable pageable) {
        return departmentRepository.findByNameContainingIgnoreCaseAndDeletedFalse(keyword, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> findAllDepartmentsWithUserCount(Pageable pageable) {
        Page<Department> departments = departmentRepository.findByDeletedFalse(pageable);
        return departments.map(this::convertToDepartmentResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> searchDepartmentsWithUserCount(String keyword, Pageable pageable) {
        Page<Department> departments = departmentRepository.findByNameContainingIgnoreCaseAndDeletedFalse(keyword, pageable);
        return departments.map(this::convertToDepartmentResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return departmentRepository.existsByNameAndDeletedFalse(name);
    }
    
    @Override
    public void initializeDefaultDepartments() {
        // 初始化默认部门
        initDepartmentIfNotExists("摄影部", "负责活动摄影、图片拍摄等工作");
        initDepartmentIfNotExists("采编部", "负责新闻采集、内容编辑等工作");
        initDepartmentIfNotExists("审核部", "负责内容审核、质量把控等工作");
        initDepartmentIfNotExists("宣传部", "负责对外宣传、推广等工作");
    }
    
    private void initDepartmentIfNotExists(String name, String description) {
        if (!departmentRepository.existsByNameAndDeletedFalse(name)) {
            Department department = new Department(name);
            department.setDescription(description);
            departmentRepository.save(department);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public DepartmentStatistics getDepartmentStatistics() {
        long totalDepartments = departmentRepository.countByDeletedFalse();
        
        // 简化统计，只返回总数
        return new DepartmentStatistics(
            totalDepartments, 0, 0, 0, 0, 0
        );
    }
    
    /**
     * 转换Department为DepartmentResponse
     */
    private DepartmentResponse convertToDepartmentResponse(Department department) {
        long userCount = department.getUsers() != null ? department.getUsers().size() : 0;
        return new DepartmentResponse(
            department.getId(),
            department.getName(),
            department.getDescription(),
            userCount,
            department.getCreatedAt(),
            department.getUpdatedAt()
        );
    }
}
