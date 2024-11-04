package com.lt.questnest.pubsub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisMessagePublisher {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 发布消息到channel
    public String publish(String channel, String body) {
        try {
            redisTemplate.convertAndSend(channel, body);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
}
