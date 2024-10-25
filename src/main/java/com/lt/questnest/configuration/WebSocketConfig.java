package com.lt.questnest.configuration;

import com.lt.questnest.interceptor.CustomHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private CustomWebSocketHandler customWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(customWebSocketHandler, "/chat")
                .setAllowedOrigins("http://localhost:8080","http://127.0.0.1:5500","http://192.168.180.131:8080")
                .addInterceptors(new CustomHandshakeInterceptor());
        //.withSockJS(); // 添加拦截器
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean factory = new ServletServerContainerFactoryBean();
        factory.setMaxTextMessageBufferSize(1048576);  // 设置更大的文本消息缓冲区大小
        factory.setMaxBinaryMessageBufferSize(1048576); // 设置更大的二进制消息缓冲区大小
        return factory;
    }
}

