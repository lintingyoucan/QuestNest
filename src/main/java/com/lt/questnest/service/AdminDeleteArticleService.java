package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AdminDeleteArticleService {

    Map<String,String> failToArticle(String account, Integer articleId, String reason);
}
