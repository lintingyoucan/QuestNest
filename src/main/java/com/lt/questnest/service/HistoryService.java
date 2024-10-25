package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface HistoryService {

    Map<String, Object> addHistory(String email, Integer articleId);

    Map<String, Object> showHistory(String email);
}
