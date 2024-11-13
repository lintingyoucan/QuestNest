package com.lt.questnest.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig 类实现了 WebMvcConfigurer 接口，并注册了 LoginInterceptor 拦截器。
 * 通过该配置，拦截器会对所有请求进行拦截，但排除了登录和注册页面，确保用户在访问其他页面时必须先登录。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 前后端分离，跨域处理
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 配置 API 路径，配置需要开放 CORS 的接口路径
                .allowedOrigins("http://127.0.0.1:5500",
                        "http://192.168.180.131:8080",
                        "http://127.0.0.1:8080",
                        "http://localhost:8080",
                        "http://localhost:63342")  // 允许访问后端 API 的前端域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的 HTTP 方法
                //.allowedHeaders("*")  // 允许的请求头
                .allowedHeaders("Authorization", "Content-Type")
                .allowCredentials(true);  // 是否允许携带认证信息（如 Cookie）
    }

}
