package com.hyperformancelabs.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map URL /uploads/** đến thư mục vật lý "uploads/" trong root của project
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + Paths.get("uploads").toAbsolutePath().toString() + "/");
    }
}
