package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AdminCheckQuestionService {

    Map<String,Object> getQuestion();
}
