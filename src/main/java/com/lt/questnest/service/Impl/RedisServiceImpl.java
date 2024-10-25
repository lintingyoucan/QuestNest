package com.lt.questnest.service.Impl;

import com.lt.questnest.controller.UserController;
import com.lt.questnest.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public void setVerificationCode(String email, String code) {
        long expiration = 5;  // 过期时间设置为5分钟
        // email 作为键，将 code 作为值
        logger.info("Redis 中 key-value: {},{}", email,code);
        redisTemplate.opsForValue().set(email, code, expiration, TimeUnit.MINUTES);
    }

    public String getVerificationCode(String email) {
        // 检查 Redis 中是否存在该键
        if (!redisTemplate.hasKey(email)) {
            logger.info("Redis 中没有找到 key: {}", email);  // 如果不存在，打印日志
            return null;  // 返回 null，表示没有找到验证码
        }

        // 如果存在，则获取对应的验证码
        return redisTemplate.opsForValue().get(email);
    }


    public void removeVerificationCode(String email){
        redisTemplate.delete(email);
    }


    // 将通知内容存入Redis
    public void addNotification(String key, String notification) {
        redisTemplate.opsForList().leftPush(key, notification); // 从左侧存储信息，可以将最新信息先弹出来
        redisTemplate.expire(key, 7, TimeUnit.DAYS);  // 设置通知的过期时间，比如保留7天，如果用户在线，可以即时推送消息，如果用户不在线，等用户上线时，也可以马上将消息推送给用户
    }


    // 获取通知内容
    public List<String> getNotifications(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }


    // 清空通知
    public void clearNotifications(String key) {
        redisTemplate.delete(key);
    }
}
