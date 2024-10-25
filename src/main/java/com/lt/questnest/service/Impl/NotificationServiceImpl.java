package com.lt.questnest.service.Impl;

import com.lt.questnest.controller.UserController;
import com.lt.questnest.service.NotificationService;
import com.lt.questnest.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private RedisService redisService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    @Async("taskExecutor")
    public void sendCommentNotification(String articleUserEmail, String commentUsername, String title) {
        // 获取当前的日期和时间
        LocalDateTime currentDateTime = LocalDateTime.now();
        // 格式化为自定义格式，例如：yyyy-MM-dd HH:mm
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDateTime = currentDateTime.format(formatter);

        // 生成通知内容：评论者评论了你的回答
        String notificationContent = String.format("用户 %s 评论了您的回答：%s 时间 %s",
                commentUsername, title, formattedDateTime);

        // 存储通知到 Redis
        String redisKey = "notifications:" + articleUserEmail;
        redisService.addNotification(redisKey, notificationContent);

    }

    @Async("taskExecutor")
    public void sendReplyCommentNotification(String parentCommentUserEmail, String commentUsername, String title) {
        // 获取当前的日期和时间
        LocalDateTime currentDateTime = LocalDateTime.now();
        // 格式化为自定义格式，例如：yyyy-MM-dd HH:mm
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDateTime = currentDateTime.format(formatter);

        // 生成通知内容：评论者评论了你的回答
        String notificationContent = String.format("用户 %s 回复了您回答下的评论：%s 时间 %s",
                commentUsername, title, formattedDateTime);

        // 存储通知到 Redis
        String redisKey = "notifications:" + parentCommentUserEmail;
        redisService.addNotification(redisKey, notificationContent);

    }

    @Async("taskExecutor")
    public void failToArticleNotification(String email, String content, String title,String reason) {

        // 生成通知内容，包含问题标题、回答内容和违规原因
        String notificationContent = String.format("回答违规提示\n" +
                        "问题：%s\n" +
                        "回答：%s\n" +
                        "违规原因：%s",
                title, content, reason);

        // 存储通知到 Redis
        String redisKey = "notifications:" + email;
        redisService.addNotification(redisKey, notificationContent);
    }

    @Async("taskExecutor")
    public void failToQuestionNotification(String email, String title, String content,String reason) {

        // 生成通知内容，包含问题标题、回答内容和违规原因
        String notificationContent = String.format("问题违规提示\n" +
                        "标题：%s\n" +
                        "内容：%s\n" +
                        "违规原因：%s",
                title, content, reason);
        //logger.info("问题通知内容:{}",notificationContent);
        // 存储通知到 Redis
        String redisKey = "notifications:" + email;
        redisService.addNotification(redisKey, notificationContent);
    }
}
