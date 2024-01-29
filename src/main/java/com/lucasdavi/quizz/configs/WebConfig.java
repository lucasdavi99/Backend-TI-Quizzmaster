package com.lucasdavi.quizz.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://lucasdavi99.github.io") // URL do seu frontend
                .allowedMethods("GET")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}