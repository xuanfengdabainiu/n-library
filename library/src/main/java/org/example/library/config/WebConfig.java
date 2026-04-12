package org.example.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✨ 任意门：当网页请求 /ai-faces/qwer.jpg 时，Java 会去 Python 的文件夹里拿图！
        // 🚨 请务必把下面的 file: 后面的路径，换成你 Python 项目里 users 文件夹的【绝对路径】！
        // 比如：file:D:/develop/python_project/users/ （注意最后一定要有斜杠 /）
        registry.addResourceHandler("/ai-faces/**")
                .addResourceLocations("file:D:/yolo/face1/users/");
    }
}