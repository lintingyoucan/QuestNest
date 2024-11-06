package com.lt.questnest.service.Impl;

import com.lt.questnest.entity.CommentAreaMute;
import com.lt.questnest.entity.User;
import com.lt.questnest.mapper.CommentAreaMuteMapper;
import com.lt.questnest.mapper.ArticleMapper;
import com.lt.questnest.mapper.UserMapper;
import com.lt.questnest.service.CommentAreaMuteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CommentAreaMuteServiceImpl implements CommentAreaMuteService {

    @Autowired
    CommentAreaMuteMapper commentAreaMuteMapper;

    @Autowired
    ArticleMapper articleMapper;

    @Autowired
    UserMapper userMapper;

    private static final Logger logger = LoggerFactory.getLogger(CommentAreaMuteServiceImpl.class);

    // 关闭评论区
    public Map<String, Object> closeCommentArea(Integer articleId,String email) {

        Map<String,Object> result = new HashMap<>();

        // 对传入参数判空处理
        if (articleId == null || articleId <= 0){
            result.put("status", "error");
            result.put("msg","回答不能为空!");
            return result;
        }


        try {
            // 如果email不为null，说明是用户操作
            // 找出用户的ID
            // 找出文章作者ID
            // 比较是不是同一个人，以防用户非法操作，关闭别人文章评论区
            if (email != null && !(email.isEmpty())){
                User user = userMapper.getUserByEmail(email);
                Integer authorId = articleMapper.findAuthor(articleId);
                if (authorId != user.getId()){
                    result.put("status","error");
                    result.put("msg","用户非法操作");
                    return result;
                }
            }

            // 判断回答是否存在
            String content = articleMapper.findContent(articleId);
            if (content == null || content.isEmpty()){
                result.put("status", "error");
                result.put("msg","回答不存在!");
                return result;
            }

            // 判断是否已关闭该评论区
            result = isMute(articleId);
            if (result.containsValue("error")){
                return result;
            }

            // 创建实例
            CommentAreaMute commentAreaMute = new CommentAreaMute();
            commentAreaMute.setArticleId(articleId);

            // 保存数据
            Integer addResult = commentAreaMuteMapper.add(commentAreaMute);
            if (addResult == null || addResult <= 0){
                result.put("status","error");
                result.put("msg","保存数据失败");
                return result;
            }

        } catch (Exception e) {

            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        // 成功
        result.put("status", "success");
        return result;
    }


    // 检查评论区是否被禁言
    public Map<String, Object> isMute(Integer articleId){

        logger.info("进入isMute方法");
        Map<String,Object> result = new HashMap<>();

        // 检查是否禁言
        Integer exist = commentAreaMuteMapper.findByArticleId(articleId);
        if (exist != 0){
            result.put("status", "error");
            result.put("msg","该评论区已关闭!");
            logger.info("退出isMute方法");
            return result;
        }

        result.put("status","success");
        result.put("msg","该评论区未关闭!");
        logger.info("退出isMute方法");
        return result;
    }

    // 取消关闭评论区
    public Map<String, Object> cancelCloseCommentArea(Integer articleId,String email) {

        Map<String,Object> result = new HashMap<>();

        // 对传入参数判空处理
        if (articleId == null || articleId <= 0){
            result.put("status", "error");
            result.put("msg","回答不能为空!");
            return result;
        }

        try {
            // 如果email不为null，说明是用户操作
            // 找出用户的ID
            // 找出文章作者ID
            // 比较是不是同一个人，以防用户非法操作，关闭别人文章评论区
            if (email != null && !(email.isEmpty())){
                User user = userMapper.getUserByEmail(email);
                Integer authorId = articleMapper.findAuthor(articleId);
                if (authorId != user.getId()){
                    result.put("status","error");
                    result.put("msg","用户非法操作");
                    return result;
                }
            }

            // 判断回答是否存在
            String content = articleMapper.findContent(articleId);
            if (content == null || content.isEmpty()){
                result.put("status", "error");
                result.put("msg","回答不存在!");
                return result;
            }

            // 判断是否已关闭该评论区
            result = isMute(articleId);
            if (result.containsValue("success")){ // 评论区未被禁言
                result.remove("status","success");
                result.put("status","error");
                return result;
            }

            // 删除禁言记录，保存数据
            Integer deleteResult = commentAreaMuteMapper.delete(articleId);
            if (deleteResult == null || deleteResult <= 0){
                result.put("status","error");
                result.put("msg","删除禁言记录失败");
                return result;
            }

        } catch (Exception e) {

            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        // 成功
        result.put("status", "success");
        return result;
    }

}
