package org.example.library.config;

import org.springframework.beans.factory.annotation.Value; // ✨ 新增导入
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✨ 核心修改：从配置文件动态读取图片存放路径
    @Value("${face.image.path}")
    private String faceImagePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✨ 任意门：当网页请求 /ai-faces/xxx.jpg 时
        // Java 会根据当前环境（dev/prod）去对应的文件夹拿图
        registry.addResourceHandler("/ai-faces/**")
                .addResourceLocations("file:" + faceImagePath);
    }
}