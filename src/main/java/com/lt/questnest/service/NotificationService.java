package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public interface NotificationService {

    void sendCommentNotification(String articleUserEmail, String commentUsername, String title);

    void sendReplyCommentNotification(String parentCommentUserEmail, String commentUsername, String title);

    void failToArticleNotification(String email, String content, String title,String reason);

    void failToQuestionNotification(String email, String title, String content,String reason);
}
