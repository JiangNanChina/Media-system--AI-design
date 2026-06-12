package com.example.photography.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 部门实体
 */
@Entity
@Table(name = "departments")
public class Department extends BaseEntity {
    
    @NotBlank(message = "部门名称不能为空")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @JsonIgnore  // 防止JSON序列化时的循环引用
    private List<User> users;
    
    // Constructors
    public Department() {}
    
    public Department(String name) {
        this.name = name;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<User> getUsers() {
        return users;
    }
    
    public void setUsers(List<User> users) {
        this.users = users;
    }
}
