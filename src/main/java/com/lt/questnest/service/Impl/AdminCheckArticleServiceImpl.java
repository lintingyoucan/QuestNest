package com.lt.questnest.service.Impl;

import com.lt.questnest.entity.AdminCheckArticle;
import com.lt.questnest.mapper.AdminCheckArticleMapper;
import com.lt.questnest.mapper.ArticleMapper;
import com.lt.questnest.service.AdminCheckArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminCheckArticleServiceImpl implements AdminCheckArticleService {

    @Autowired
    ArticleMapper articleMapper;

    @Autowired
    AdminCheckArticleMapper adminCheckArticleMapper;

    // 获取未审核回答
    public Map<String,Object> getArticle(){

        Map<String,Object> result = new HashMap<>();

        List<AdminCheckArticle> articles = adminCheckArticleMapper.get();
        if (articles == null || articles.isEmpty()){
            result.put("status","success");
            result.put("article",new ArrayList<>());
            return result;
        }
        List<Map<String,Object>> articleList = new ArrayList<>();
        for (AdminCheckArticle adminCheckArticle : articles) {
            Map<String,Object> articleItem = new HashMap<>();
            articleItem.put("articleId",adminCheckArticle.getArticleId());
            // 获取文章内容
            String content = articleMapper.findContent(adminCheckArticle.getArticleId());
            articleItem.put("content",content);

            articleList.add(articleItem);
        }

        result.put("article",articleList);
        result.put("status","success");
        return result;
    }
}
