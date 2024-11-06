package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public interface QuestionService {

    Map<String,Object> addQuestion(String email, String title, String content, Set<String> topics);

    Map<String, Object> search(String keyword);

    Map<String,String> updateQuestion(Integer questionId,String email,String title,String content,Set<String> topics);

    Map<String,Object> searchByAI(String keyword);

    Map<String,Object> getIllegalQuestion(String email);

    Integer getAnswerNumber(String title);
}
