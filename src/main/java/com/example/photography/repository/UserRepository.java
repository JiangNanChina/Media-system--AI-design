package com.example.photography.repository;

import com.example.photography.model.entity.User;
import com.example.photography.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsernameAndDeletedFalse(String username);
    
    /**
     * 根据用户名查找用户（带部门信息）
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department WHERE u.username = :username AND u.deleted = false")
    Optional<User> findByUsernameAndDeletedFalseWithDepartment(@Param("username") String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmailAndDeletedFalse(String email);

    /**
     * 根据邮箱查找用户（忽略大小写）
     */
    Optional<User> findByEmailIgnoreCaseAndDeletedFalse(String email);
    
    /**
     * 根据ID查找用户（带部门信息）
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department WHERE u.id = :id AND u.deleted = false")
    Optional<User> findByIdWithDepartment(@Param("id") Long id);
    

    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsernameAndDeletedFalse(String username);
    
    /**
     * 检查用户名是否存在（不考虑删除状态）
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmailAndDeletedFalse(String email);
    

    
    /**
     * 根据角色查找用户
     */
    List<User> findByRoleAndDeletedFalse(UserRole role);
    
    /**
     * 根据部门ID查找用户
     */
    List<User> findByDepartmentIdAndDeletedFalse(Long departmentId);
    
    /**
     * 分页查询用户
     */
    Page<User> findByDeletedFalse(Pageable pageable);
    
    /**
     * 查询所有未删除用户（带排序）
     */
    List<User> findByDeletedFalse(Sort sort);
    
    /**
     * 分页查询用户（带部门信息）
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department WHERE u.deleted = false")
    Page<User> findByDeletedFalseWithDepartment(Pageable pageable);
    
    /**
     * 查询所有用户（带部门信息）
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department WHERE u.deleted = false ORDER BY u.createdAt DESC")
    List<User> findByDeletedFalseWithDepartmentList();
    
    /**
     * 根据关键字搜索用户
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND " +
           "(u.username LIKE %:keyword% OR u.realName LIKE %:keyword% OR " +
           "u.email LIKE %:keyword%)")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据关键字搜索用户（带部门信息）
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department WHERE u.deleted = false AND " +
           "(u.username LIKE %:keyword% OR u.realName LIKE %:keyword% OR " +
           "u.email LIKE %:keyword%)")
    Page<User> searchUsersWithDepartment(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 组合条件搜索用户（带部门信息）
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department d WHERE u.deleted = false " +
           "AND (:keyword IS NULL OR u.username LIKE %:keyword% OR u.realName LIKE %:keyword% OR u.email LIKE %:keyword%) " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:departmentId IS NULL OR u.department.id = :departmentId) " +
           "AND (:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> searchUsersWithFilters(@Param("keyword") String keyword,
                                       @Param("role") UserRole role,
                                       @Param("departmentId") Long departmentId,
                                       @Param("enabled") Boolean enabled,
                                       Pageable pageable);
    
    /**
     * 根据部门和角色查找用户
     */
    List<User> findByDepartmentIdAndRoleAndDeletedFalse(Long departmentId, UserRole role);
    
    /**
     * 根据角色查找用户（包含部门信息）
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department WHERE u.role = :role AND u.deleted = false")
    List<User> findByRoleWithDepartment(@Param("role") UserRole role);
    
    /**
     * 根据部门ID查找用户（包含部门信息）
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department WHERE u.department.id = :departmentId AND u.deleted = false")
    List<User> findByDepartmentIdWithDepartment(@Param("departmentId") Long departmentId);
    
    /**
     * 统计用户数量
     */
    long countByDeletedFalse();
    
    /**
     * 统计各角色用户数量
     */
    long countByRoleAndDeletedFalse(UserRole role);
    
    /**
     * 获取所有激活的未删除用户（按姓名排序，包含部门信息）
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department WHERE u.enabled = true AND u.deleted = false ORDER BY u.realName ASC")
    List<User> findByEnabledTrueAndDeletedFalseOrderByRealNameAsc();
}
