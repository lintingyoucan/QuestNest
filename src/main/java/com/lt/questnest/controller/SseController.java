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

    public static void remove(String account) {
        RedisMessageSubscriber.emitters.remove(account);
        logger.info("移除连接：{}", account);
    }

    private static Runnable completionCallBack(String account) {
        return () -> {
            logger.info("结束连接：{}", account);
            remove(account);
        };
    }

    private static Runnable timeoutCallBack(String account) {
        return () -> {
            logger.info("结束连接：{}", account);
            remove(account);
        };
    }

    private static Consumer<Throwable> errorCallBack(String account) {
        return throwable -> {
            logger.info("连接异常：{}", account);
            remove(account);
        };
    }


    @GetMapping("/sse")
    public SseEmitter connect(@RequestParam("account") String account) {

        logger.info("进入connect方法");

        if (RedisMessageSubscriber.emitters.get(account) != null){
            return RedisMessageSubscriber.emitters.get(account);
        }

        // 超时时间设置为1小时
        logger.info("创建新的SseEmitter");
        SseEmitter sseEmitter = new SseEmitter(3_600_000L);
        RedisMessageSubscriber.emitters.put(account, sseEmitter);
        sseEmitter.onTimeout(timeoutCallBack(account)); // 超时
        sseEmitter.onCompletion(completionCallBack(account)); // 完成
        sseEmitter.onError(errorCallBack(account)); // 错误
        return sseEmitter;

    }

}
