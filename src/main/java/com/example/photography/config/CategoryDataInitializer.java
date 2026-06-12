package com.example.photography.config;

import com.example.photography.model.entity.EquipmentCategory;
import com.example.photography.service.EquipmentCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 设备分类数据初始化器
 */
@Component
@Order(3) // 确保在其他初始化器之后执行
public class CategoryDataInitializer implements CommandLineRunner {
    
    @Autowired
    private EquipmentCategoryService categoryService;
    
    @Override
    public void run(String... args) throws Exception {
        initializeCategories();
    }
    
    private void initializeCategories() {
        System.out.println("开始初始化设备分类数据...");
        
        // 定义默认分类
        String[][] defaultCategories = {
            {"相机", "各类相机设备，包括单反相机、微单相机等", "1"},
            {"镜头", "各类镜头，包括定焦镜头、变焦镜头等", "2"},
            {"三脚架", "各类三脚架和稳定器设备", "3"},
            {"闪光灯", "各类闪光灯和照明设备", "4"},
            {"录音设备", "各类录音和音频设备", "5"},
            {"无人机", "无人机和航拍设备", "6"},
            {"其他", "其他摄影相关设备", "7"}
        };
        
        for (String[] categoryData : defaultCategories) {
            String name = categoryData[0];
            String description = categoryData[1];
            Integer sortOrder = Integer.parseInt(categoryData[2]);
            
            try {
                // 检查分类是否已存在
                if (!categoryService.existsByName(name)) {
                    EquipmentCategory category = new EquipmentCategory(name, description, sortOrder);
                    categoryService.createCategory(category);
                    System.out.println("创建分类: " + name);
                } else {
                    System.out.println("分类已存在: " + name);
                }
            } catch (Exception e) {
                System.err.println("创建分类失败 " + name + ": " + e.getMessage());
            }
        }
        
        System.out.println("设备分类数据初始化完成");
    }
}
