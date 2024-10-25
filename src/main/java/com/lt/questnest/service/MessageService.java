package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface MessageService {


    Map<String,Object> sendMessage(String senderEmail, String receiverEmail, String content);

    Map<String,Object> getMessage(Integer conversationId);
}
