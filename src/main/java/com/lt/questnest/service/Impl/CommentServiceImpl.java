package com.lt.questnest.service.Impl;

import com.lt.questnest.controller.UserController;
import com.lt.questnest.entity.Comment;
import com.lt.questnest.entity.User;
import com.lt.questnest.entity.UserCommentLike;
import com.lt.questnest.mapper.*;
import com.lt.questnest.pubsub.RedisMessagePublisher;
import com.lt.questnest.service.CommentService;
import com.lt.questnest.service.InformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    CommentMapper commentMapper;

    @Autowired
    ArticleMapper articleMapper;

    @Autowired
    QuestionMapper questionMapper;

    @Autowired
    UserCommentLikeMapper userCommentLikeMapper;

    @Autowired
    RedisMessagePublisher redisMessagePublisher;

    @Autowired
    InformService informService;


    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 评论文章、回复评论
    @Transactional
    public Map<String, Object> addComment(String email, Integer articleId, Integer parentCommentId, String content) {

        Map<String, Object> result = new HashMap<>();
        // 判断参数是否为空或有效
        if (articleId <= 0) {
            result.put("status", "error");
            result.put("msg", "参数无效");
            return result;
        }

        if (content == null || content.isEmpty()) {
            result.put("status", "error");
            result.put("msg", "内容不能为空");
            return result;
        }

        // 判断文章是否存在
        String article = articleMapper.findContent(articleId);
        if (article == null) {
            result.put("status", "error");
            result.put("msg", "回答不存在");
            return result;
        }

        String commentUsername = null; // 评论者的用户名
        Integer commentId = 0; // 评论者添加评论后的Id
        Integer commentUserId = 0; // 评论者的用户ID
        Integer articleUserId = 0; // 评论者评论的文章作者的Id
        String articleUserEmail = null; // 评论者评论的文章作者的email，用于评论文章后通知文章作者
        Integer parentCommentUserId = 0; // 父评论的作者Id
        String parentCommentContent = null; // 父评论的内容
        String parentCommentUserEmail = null; // 父评论的作者email,用于回复评论后通知父评论作者


        // 获取评论的作者
        User commentUser = userMapper.getUserByEmail(email);
        if (commentUser == null) {
            throw new RuntimeException("从数据库获取评论作者失败");
        }
        commentUserId = commentUser.getId();
        commentUsername = commentUser.getUsername();

        // 将评论保存进数据库
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(commentUserId);
        comment.setContent(content);
        if (parentCommentId != null && parentCommentId > 0) { // 如果parentCommentId存在
            comment.setParentCommentId(parentCommentId);
        }

        logger.info("即将插入的评论数据: {}", comment);
        Integer addResult = commentMapper.addComment(comment);
        if (addResult == null || addResult <= 0) {
            throw new RuntimeException("插入comment失败");
        }
        commentId = comment.getCommentId();
        logger.info("插入后的comment: {}", comment);
        result.put("commentId", commentId); // 将commentId返回，方便定位评论

        if (parentCommentId == null || parentCommentId <= 0) { // 如果parentCommentId不存在
            // 获取文章的作者ID
            articleUserId = articleMapper.findAuthor(articleId);
            if (articleUserId == null || articleUserId <= 0) {
                throw new RuntimeException("从数据库获取文章的作者失败");
            }

            // 获取文章作者email
            articleUserEmail = userMapper.getUserById(articleUserId).getEmail();

        } else {
            // 如果parentCommentId存在
            Comment parentComment = commentMapper.findByParentCommentId(parentCommentId);
            if (parentComment == null) {
                throw new RuntimeException("获取父评论失败");
            }
            parentCommentUserId = parentComment.getUserId();
            parentCommentContent = parentComment.getContent();
            parentCommentUserEmail = userMapper.getUserById(parentCommentUserId).getEmail();
            result.put("parentCommentContent", parentCommentContent); // 将父评论的内容返回
        }


        // 异步站内通知:修改，调用redis发布消息到频道和保存进数据库
        if (parentCommentId == null || parentCommentId <= 0) { // parentCommentId不存在，评论文章

            // 如果文章作者不存在，那么不必通知，避免插入数据发生错误
            if (articleUserEmail == null || articleUserEmail.isEmpty()) {
                result.put("status", "success");
                return result;
            }

            String body = commentUsername + "评论了你的文章:" + content;
            String addInform = informService.add(commentUserId, articleUserId, body);
            String publish = redisMessagePublisher.publish(articleUserEmail, body);

            if (addInform.equals("success") && publish.equals("success")) {
                result.put("status", "success");
                return result;
            }

            throw new RuntimeException("异步站内通知失败");

        } else { // 回复父评论

            // 如果文章作者不存在，那么不必通知，避免插入数据发生错误
            if (parentCommentUserEmail == null || parentCommentUserEmail.isEmpty()) {
                result.put("status", "success");
                return result;
            }
            String body = commentUsername + "回复了你的评论:" + content;
            String addInform = informService.add(commentUserId, parentCommentUserId, body);
            String publish = redisMessagePublisher.publish(parentCommentUserEmail, body);

            if (addInform.equals("success") && publish.equals("success")) {
                result.put("status", "success");
                return result;
            }

            throw new RuntimeException("异步站内通知失败");
        }


    }

    // 点赞评论
    @Transactional
    public Map<String, String> agreeComment(String email, int commentId) {

        Map<String, String> result = new HashMap<>();
        // 检查评论是否存在
        Comment comment = commentMapper.findComment(commentId);
        if (comment == null) {
            result.put("status", "error");
            result.put("msg", "从数据库获取评论作者失败");
            return result;
        }
        // 获取用户
        User user = userMapper.getUserByEmail(email);
        // 用户存在，获取userId
        Integer userId = user.getId();

        // 判断用户对评论是否进行过赞同
        result = agreeToComment(userId, commentId);
        if (result.containsValue("error")) {
            return result;
        }

        // 如果没进行操作，那么将用户对文章的赞同操作保存进数据库
        // comment的like+1,user_comment_like 记录行为
        Integer addResult = userCommentLikeMapper.add(userId, commentId);
        if (addResult == null || addResult <= 0) {
            throw new RuntimeException("添加用户点赞评论记录失败");
        }

        Integer updateResult = commentMapper.addLike(commentId);
        if (updateResult == null || updateResult <= 0) {
            throw new RuntimeException("修改评论点赞数量失败");
        }

        result.put("status", "success");
        return result;
    }

    // 对评论的赞同
    private Map<String, String> agreeToComment(int userId, int commentId) {
        logger.info("进入agreeToComment方法");
        Map<String, String> result = new HashMap<>();
        // 赞同
        UserCommentLike userCommentLike = userCommentLikeMapper.find(userId, commentId);
        if (userCommentLike != null) { // 用户对这篇文章已经有赞同操作
            result.put("status", "error");
            result.put("msg", "用户已赞同");
        }
        return result;
    }

    // 取消点赞评论
    @Transactional
    public Map<String, String> cancelAgreeComment(String email, int commentId) {

        Map<String, String> result = new HashMap<>();
        // 检查评论是否存在
        Comment comment = commentMapper.findComment(commentId);
        if (comment == null) {
            result.put("status", "error");
            result.put("msg", "从数据库获取评论作者失败");
            return result;
        }
        // 获取用户
        User user = userMapper.getUserByEmail(email);
        // 用户存在，获取userId
        Integer userId = user.getId();

        // 判断用户对文章是否进行操作
        // 如果用户没有赞同操作
        result = agreeToComment(userId, commentId);
        if (!(result.containsValue("error"))) {
            result.put("status", "error");
            result.put("msg", "用户没有进行赞同操作，无法取消赞同");
            return result;
        }

        // 如果用户有赞同操作，那么取消赞同，需要对comment的like-1,user_comment_like的state = 0
        Integer reduceResult = commentMapper.reduceLike(commentId);
        if (reduceResult == null || reduceResult <= 0) {
            throw new RuntimeException("取消赞同操作，减少评论点赞人数失败");
        }
        Integer deleteResult = userCommentLikeMapper.delete(userId, commentId);
        if (deleteResult == null || deleteResult <= 0) {
            throw new RuntimeException("删除用户点赞评论行为失败");
        }

        result.put("status", "success");
        return result;
    }

    // 返回评论点赞人数
    public Map<String, Object> agreeCommentNumber(int commentId) {

        Map<String, Object> result = new HashMap<>();
        try {
            // 获取点赞人数
            int like = commentMapper.like(commentId);
            result.put("status", "success");
            result.put("like", like);//返回点赞人数
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("数据库操作出现问题:{}", e.getMessage());
            result.put("status", "error");
            result.put("msg", "数据库操作失败");
        }
        return result;
    }

    // 删除评论
    public Map<String, String> deleteComment(Integer commentId, String email) {

        Map<String, String> result = new HashMap<>();
        // 参数判空
        if (commentId == null || commentId <= 0) {
            result.put("status", "error");
            result.put("msg", "articleId为空或无效");
            return result;
        }
        if (email == null || email.isEmpty()) {
            result.put("status", "error");
            result.put("msg", "email为空");
            return result;
        }

        // 判断commentId是否存在
        Comment comment = commentMapper.findComment(commentId);
        if (comment == null) {
            result.put("status", "error");
            result.put("msg", "comment不存在");
            return result;
        }

        // 判断发评论的作者和登录的作者是否相同，避免错误操作
        Integer authorId = comment.getUserId();
        Integer userId = userMapper.getUserByEmail(email).getId();
        if (userId != authorId) {
            result.put("status", "error");
            result.put("msg", "非法操作");
            return result;
        }

        // 删除评论
        try {
            Integer deleteResult = commentMapper.deleteByCommentId(commentId);
            if (deleteResult == null || deleteResult <= 0) {
                throw new RuntimeException("删除评论行为失败");
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("msg", "数据库操作失败");
            return result;
        }

        result.put("status", "success");
        return result;
    }
}
