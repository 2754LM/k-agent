package com.kano.main_data.config;

import com.kano.main_data.interceptor.SessionContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private SessionContextInterceptor sessionContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(sessionContextInterceptor)
//                .addPathPatterns("/**")
//                .excludePathPatterns("/login");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("http://localhost:63342")
//                .allowedMethods("*") //
//                .allowedHeaders("*") //
//                .allowCredentials(true)
//                .maxAge(3600);
    }

}
