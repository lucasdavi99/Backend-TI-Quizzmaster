package com.lucasdavi.quizz.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "https://tiquizzmaster.fun",
                        "http://localhost:3000",
                        "http://127.0.0.1:5500",
                        "http://localhost:5500",
                        "http://127.0.0.1:8080",
                        "http://localhost:8080",
                        "http://167.99.124.8",
                        "http://localhost:4200"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD") // 🔧 ADICIONADO HEAD
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 🔧 ADICIONADO cache de preflight
    }
}