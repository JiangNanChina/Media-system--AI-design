package com.example.photography.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 注意：已在 application.yml 中配置了 context-path: /api
        // 所以这里不需要再添加 /api 前缀，否则会导致双重前缀问题
        // configurer.addPathPrefix("/api", c -> c.getPackageName().contains("controller"));
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置API文档的访问路径
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");
        
        // 静态资源配置已移至 FileUploadConfig.java，避免重复配置冲突
        // 确保 /api/** 路径不被当作静态资源处理
        // 这里我们不添加 /api/** 的资源处理器，让它们由 Controller 处理
    }
    
    /**
     * RestTemplate Bean配置
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
