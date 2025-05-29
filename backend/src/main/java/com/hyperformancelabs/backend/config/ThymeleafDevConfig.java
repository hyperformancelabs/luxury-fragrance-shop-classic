package com.hyperformancelabs.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.io.IOException;

/**
 * Configuration for Thymeleaf hot reloading from filesystem during development
 */
@Configuration
@Profile("dev")
public class ThymeleafDevConfig {

    /**
     * Configures Thymeleaf to load templates from filesystem instead of classpath
     * This enables real-time reloading of templates without restarting the application
     */
    @Bean
    public FileTemplateResolver fileTemplateResolver(SpringTemplateEngine templateEngine) throws IOException {
        ClassPathResource applicationProperties = new ClassPathResource("application.properties");
        
        if (applicationProperties.exists()) {
            // Find the project root directory
            File sourceRoot = applicationProperties.getFile().getParentFile();
            while (sourceRoot != null && !new File(sourceRoot, "pom.xml").exists()) {
                sourceRoot = sourceRoot.getParentFile();
            }
            
            if (sourceRoot != null) {
                // Configure the file template resolver
                FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();
                fileTemplateResolver.setPrefix(sourceRoot.getPath() + "/src/main/resources/templates/");
                fileTemplateResolver.setSuffix(".html");
                fileTemplateResolver.setCacheable(false);
                fileTemplateResolver.setCharacterEncoding("UTF-8");
                fileTemplateResolver.setCheckExistence(true);
                fileTemplateResolver.setOrder(1);
                
                // Add the resolver to the template engine
                templateEngine.addTemplateResolver(fileTemplateResolver);
                
                return fileTemplateResolver;
            }
        }
        
        return null;
    }
} 