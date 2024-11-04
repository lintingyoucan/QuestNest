package com.lt.questnest.controller;

import com.lt.questnest.pubsub.RedisMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.function.Consumer;

@RestController
@CrossOrigin
public class SseController {

    private static final Logger logger = LoggerFactory.getLogger(RedisMessageSubscriber.class);

    public static void remove(String email) {
        RedisMessageSubscriber.emitters.remove(email);
        logger.info("移除连接：{}", email);
    }

    private static Runnable completionCallBack(String email) {
        return () -> {
            logger.info("结束连接：{}", email);
            remove(email);
        };
    }

    private static Runnable timeoutCallBack(String email) {
        return () -> {
            logger.info("结束连接：{}", email);
            remove(email);
        };
    }

    private static Consumer<Throwable> errorCallBack(String email) {
        return throwable -> {
            logger.info("连接异常：{}", email);
            remove(email);
        };
    }


    @GetMapping("/sse")
    public SseEmitter connect(@RequestParam("email") String email) {

        logger.info("进入connect方法");

        if (RedisMessageSubscriber.emitters.get(email) != null){
            return RedisMessageSubscriber.emitters.get(email);
        }

        // 超时时间设置为1小时
        logger.info("创建新的SseEmitter");
        SseEmitter sseEmitter = new SseEmitter(3_600_000L);
        RedisMessageSubscriber.emitters.put(email, sseEmitter);
        sseEmitter.onTimeout(timeoutCallBack(email)); // 超时
        sseEmitter.onCompletion(completionCallBack(email)); // 完成
        sseEmitter.onError(errorCallBack(email)); // 错误
        return sseEmitter;

    }

}
