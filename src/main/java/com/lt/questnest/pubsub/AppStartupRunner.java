package com.lt.questnest.pubsub;

import com.lt.questnest.service.SubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppStartupRunner implements ApplicationRunner {

    @Autowired
    private SubService subService;

    @Autowired
    private RedisMessageSubscriber redisMessageSubscriber;

    @Override
    public void run(ApplicationArguments args) {
        // 读取所有订阅信息并自动订阅
        List<String> channels = subService.getChannel();
        for (String channel : channels) {
            redisMessageSubscriber.subscribe(channel);
        }
    }

}
