package com.lt.questnest.service.Impl;

import com.lt.questnest.entity.AdminDeleteArticle;
import com.lt.questnest.mapper.*;
import com.lt.questnest.service.AdminDeleteArticleService;
import com.lt.questnest.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;


@Service
public class AdminDeleteArticleServiceImpl implements AdminDeleteArticleService {

    @Autowired
    AdminDeleteArticleMapper adminDeleteArticleMapper;

    @Autowired
    ArticleMapper articleMapper;

    @Autowired
    QuestionMapper questionMapper;

    @Autowired
    AdminMapper adminMapper;

    @Autowired
    NotificationService notificationService;

    @Autowired
    UserMapper userMapper;


    @Transactional
    public Map<String,String> failToArticle(String account, Integer articleId, String reason){

        Map<String,String> result = new HashMap<>();
        // 参数判空
        if (articleId == null || articleId <= 0){
            result.put("status","error");
            result.put("msg","articleId为空或无效");
            return result;
        }
        if (reason == null || reason.isEmpty()){
            result.put("status","error");
            result.put("msg","原因不能为空");
            return result;
        }


        String userEmail;
        String content;
        String title;

        // 修改回答状态、添加记录
        try {

            // 获取文章内容，判断articleId是否存在和审核是否通过（如果state为0，说明审核不通过，不用再添加记录）
            content = articleMapper.findContent(articleId);
            if (content == null || content.isEmpty()){
                result.put("status","error");
                result.put("msg","回答不存在或审核已经不给通过,无法打回重修");
                return result;
            }

            // 获取作者ID
            Integer userId = articleMapper.findAuthor(articleId);
            // 获取作者email
            userEmail = userMapper.getUserById(userId).getEmail();

            // 获取问题title
            Integer questionId = articleMapper.findQuestionId(articleId);
            title = questionMapper.findQuestionTitle(questionId);

            // 找出管理员ID
            Integer adminId = adminMapper.findByAccount(account).getAdminId();
            // 创建实例
            AdminDeleteArticle adminDeleteArticle = new AdminDeleteArticle();
            adminDeleteArticle.setAdminId(adminId);
            adminDeleteArticle.setArticleId(articleId);
            adminDeleteArticle.setReason(reason);

            Integer addResult = adminDeleteArticleMapper.add(adminDeleteArticle);
            if (addResult == null || addResult <= 0){
                result.put("status","error");
                result.put("msg","添加记录失败，回答无法打回重修");
                return result;
            }

            Integer updateResult = articleMapper.updateArticleState(articleId);
            if (updateResult == null || updateResult <= 0){
                result.put("status","error");
                result.put("msg","回答打回重修失败");
                return result;
            }

        } catch (Exception e) {
            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        // 异步通知作者重修回答，给出违规原因
        notificationService.failToArticleNotification(userEmail,content,title,reason);

        result.put("status","success");
        return result;
    }
}
