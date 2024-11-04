package com.lt.questnest.configuration;

import com.lt.questnest.pubsub.RedisMessageSubscriber;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // 一个用于监听 Redis 消息的容器，主要用于实现 Redis 的发布/订阅功能
    // redisConnectionFactory:这个参数是通过依赖注入注入的，负责创建与 Redis 服务器的连接
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer result = new RedisMessageListenerContainer();
        result.setConnectionFactory(redisConnectionFactory);

        return result;
    }

    @Bean("redisTemplate")
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> result = new RedisTemplate<>();
        result.setConnectionFactory(factory); // 将 Redis 连接工厂设置到 RedisTemplate 上，以便与 Redis 服务器进行通信

        result.setKeySerializer(stringRedisSerializer());
        result.setHashKeySerializer(stringRedisSerializer());

        result.setValueSerializer(stringRedisSerializer());
        result.setHashValueSerializer(stringRedisSerializer());
        return result;
    }

    @Bean
    public RedisMessageSubscriber redisMessageListener() {
        return new RedisMessageSubscriber(stringRedisSerializer());
    }

    // 负责在 Redis 操作中进行键和值的序列化和反序列化
    @Bean
    public StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer();
    }
}
