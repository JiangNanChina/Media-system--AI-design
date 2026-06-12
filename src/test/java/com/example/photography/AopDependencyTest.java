package com.example.photography;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * AOP依赖测试
 * 用于验证Spring AOP依赖是否正确配置
 * 使用 H2 内存数据库进行测试，不需要外部 MySQL
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.show-sql=false"
})
public class AopDependencyTest {
    
    @Test
    public void contextLoads() {
        // 如果Spring上下文能够成功加载，说明AOP依赖问题已解决
        // 这个测试使用内存数据库，不需要外部MySQL
    }
}
