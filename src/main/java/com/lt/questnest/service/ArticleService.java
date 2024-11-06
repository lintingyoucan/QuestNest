package com.lt.questnest.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public interface ArticleService {
    Map<String,Object> addArticle(String email,String title,String content);

    Map<String,String> agreeArticle(String email,int articleId);

    Map<String,String> disagreeArticle(String email,int articleId);

    Map<String,String> cancelAgreeArticle(String email,int articleId);

    Map<String,String> cancelDisagreeArticle(String email,int articleId);

    Map<String,Object> agreeArticleNumber(int articleId);

    Map<String,String> updateArticle(Integer articleId,String email, String content);

    Map<String,String> updateArticleState(Integer articleId, String email);

    Map<String,Object> showArticleContent(int articleId);

    Map<String,Object> getIllegalArticle(String email);

}
