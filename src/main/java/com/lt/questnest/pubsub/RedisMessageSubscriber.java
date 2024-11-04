package com.lt.questnest.pubsub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RedisMessageSubscriber implements MessageListener {

    private StringRedisSerializer stringRedisSerializer;

    @Autowired
    private RedisMessageListenerContainer container;

    public static final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private Map<String, MessageListener> registeredListener = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(RedisMessageSubscriber.class);

    public RedisMessageSubscriber(StringRedisSerializer stringRedisSerializer) {
        this.stringRedisSerializer = stringRedisSerializer;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = stringRedisSerializer.deserialize(message.getChannel());
        String body = stringRedisSerializer.deserialize(message.getBody());
        // 将接收到的消息推送到 SSE
        // sseController.sendMessage(channel,body);
        SseEmitter emitter = emitters.get(channel);
        if (emitter != null) {
            try {
                // 发送消息到客户端
                emitter.send(body);
                // 保存到数据库

                // 统计未读消息条数

            } catch (IOException e) {
                logger.error("发送消息失败: {}", e.getMessage());
                emitter.completeWithError(e);
            }
        }else {
            logger.warn("没有找到订阅者，频道: {}", channel);
        }

    }

    // 订阅信息
    public String subscribe(String channel) {

        MessageListener listener = registeredListener.computeIfAbsent(channel, ch -> new RedisMessageSubscriber(stringRedisSerializer));
        container.addMessageListener(listener, new ChannelTopic(channel));
        return "ok";
    }

    // 取消订阅信息
    public String unsubscribe(String channel) {
        MessageListener messageListener = registeredListener.get(channel);
        if (messageListener != null) {
            emitters.remove(channel);
            container.removeMessageListener(messageListener, new ChannelTopic(channel));
        }
        return "ok";
    }
}
