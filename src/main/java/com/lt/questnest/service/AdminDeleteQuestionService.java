package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AdminDeleteQuestionService {

    Map<String,String> failToQuestion(String account, Integer questionId, String reason);
}
