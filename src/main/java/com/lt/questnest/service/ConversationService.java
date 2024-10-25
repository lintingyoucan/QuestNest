package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface ConversationService {

    Map<String,Object> getConversation(String email);

    Map<String, Object> deleteConversation(String email, Integer conversationId);
}
