package com.lt.questnest.service.Impl;

import com.lt.questnest.controller.UserController;
import com.lt.questnest.entity.*;
import com.lt.questnest.mapper.*;
import com.lt.questnest.pubsub.RedisMessagePublisher;
import com.lt.questnest.service.ArticleService;
import com.lt.questnest.service.FileService;
import com.lt.questnest.service.InformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    QuestionMapper questionMapper;

    @Autowired
    ArticleMapper articleMapper;

    @Autowired
    FileService fileService;

    @Autowired
    UserArticleLikeMapper userArticleLikeMapper;

    @Autowired
    UserArticleDislikeMapper userArticleDislikeMapper;

    @Autowired
    RedisMessagePublisher redisMessagePublisher;

    @Autowired
    InformService informService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * 添加回答
     * 20240920
     * @param email
     * @param title
     * @param content
     * @return
    **/
    @Transactional
    public Map<String,Object> addArticle(String email,String title,String content){

        Map<String,Object> result = new HashMap<>();

        // 对参数判空
        if (content == null || content.isEmpty()){
            result.put("status","error");
            result.put("msg","content n不能为空");
            return result;
        }

        // 获取回答用户
        User user = userMapper.getUserByEmail(email);
        if (user == null || !(user.isState())) { // 如果用户不存在或者已注销
            result.put("status","error");
            result.put("msg","用户不存在或已注销");
            return result;
        }
        Integer userId = user.getId(); // 用户存在，获取userId
        String username = user.getUsername(); // 获取回答的作者名字

        Question question = questionMapper.findByTitle(title);
        Integer questionId = question.getQuestionId(); // 获取问题的ID
        Integer questionUserId = question.getUserId(); // 获取问题的作者ID

        // 这里要特别注意用户是否已经注销，以防出现null错误
        String questionUserEmail = userMapper.getUserById(questionUserId).getEmail(); // 获取问题的作者email

        // 将article放入数据库
        Article article = new Article(questionId,userId,content);
        Integer addResult = articleMapper.addArticle(article);

        // 处理数据库返回结果
        if (addResult==null || addResult <= 0){
            throw new RuntimeException("回答添加失败!");
        }

        // 获取文章的ID返回给前端，当看到这篇文章时可以通过文章的ID锁定
        int articleId = article.getArticleId();
        result.put("articleId",articleId);

        // 如果用户存在，异步通知用户：有人回复问题
        if (questionUserEmail == null || questionUserEmail.isEmpty()){
            result.put("status","success");
            return result;
        }
        String body = username + "回复了你的问题:" + content;
        String save = informService.add(userId,questionUserId,body);
        String publish = redisMessagePublisher.publish(questionUserEmail,body);
        if (save.equals("success") && publish.equals("success")) {
            result.put("status", "success");
            return result;
        }
        throw new RuntimeException("异步站内通知失败!");

    }


    /**
     * 赞同文章，事务操作，两个数据库要么一起执行，要么都不执行
     * 20240920
     * @param email
     * @param articleId
     * @return
     */
    @Transactional
    public Map<String,String> agreeArticle(String email,int articleId){

        Map<String,String> result = new HashMap<>();
        // 获取用户
        User user = userMapper.getUserByEmail(email);
        if (user == null || !(user.isState())) { // 如果用户不存在或者已注销
            result.put("status","error");
            result.put("msg","用户不存在或已注销");
            return result;
        }
        // 用户存在，获取userId
        Integer userId = user.getId();

        // 判断用户对文章是否进行操作
        // 赞同
        result = agreeToArticle(userId,articleId);
        if (result.containsValue("error")){
            return result;
        }
        // 反对
        result = disagreeToArticle(userId,articleId);
        if (result.containsValue("error")){
            return result;
        }

        // 如果没进行操作，那么将用户对文章的赞同操作保存进数据库
        // article的like+1,user_article_like 记录行为
        Integer addResult = userArticleLikeMapper.add(userId,articleId);
        if (addResult == null || addResult <= 0){
            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        Integer updateResult = articleMapper.addLike(articleId);
        if (updateResult == null || updateResult <= 0){
            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        result.put("status","success");
        result.put("msg","数据库操作成功");
        return result;
    }

    /**
     * 反对文章
     * @param email
     * @param articleId
     * @return
     */
    @Transactional
    public Map<String,String> disagreeArticle(String email,int articleId){

        Map<String,String> result = new HashMap<>();
        // 获取用户
        User user = userMapper.getUserByEmail(email);
        // 用户存在，获取userId
        Integer userId = user.getId();

        // 判断用户对文章是否进行操作
        // 赞同
        result = agreeToArticle(userId,articleId);
        if (result.containsValue("error")){
            return result;
        }
        // 反对
        result = disagreeToArticle(userId,articleId);
        if (result.containsValue("error")){
            return result;
        }

        // 如果没进行操作，那么将用户对文章的赞同操作保存进数据库
        // article的dislike+1,user_article_dislike 记录行为
        Integer addResult = userArticleDislikeMapper.add(userId,articleId);
        if (addResult == null || addResult <= 0){
            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        Integer updateResult = articleMapper.addDislike(articleId);
        if (updateResult == null || updateResult <= 0){
            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        result.put("status","success");
        result.put("msg","数据库操作成功");
        return result;
    }


    // 对文章赞同
    private Map<String,String> agreeToArticle(int userId,int articleId){
        logger.info("进入agreeToArticle方法");
        Map<String,String> result = new HashMap<>();
        // 赞同
        UserArticleLike userArticleLike = userArticleLikeMapper.find(userId,articleId);
        if (userArticleLike != null){ // 用户对这篇文章已经有赞同操作
            result.put("status","error");
            result.put("msg","用户已赞同");
        }
        return result;
    }

    // 对文章反对
    private Map<String,String> disagreeToArticle(int userId,int articleId) {
        logger.info("进入disagreeToArticle方法");
        Map<String,String> result = new HashMap<>();
        // 反对
        UserArticleDislike userArticleDislike = userArticleDislikeMapper.find(userId,articleId);
        if (userArticleDislike != null){ // 用户对这篇文章已经有反对操作
            result.put("status","error");
            result.put("msg","用户已反对");
        }
        return result;
    }

    /**
     * 取消赞同文章:对两个数据库进行操作
     * 20240920
     * @param email
     * @param articleId
     * @return
     */
    @Transactional
    public Map<String,String> cancelAgreeArticle(String email,int articleId){

        Map<String,String> result = new HashMap<>();
        // 获取用户
        User user = userMapper.getUserByEmail(email);
        if (user == null || !(user.isState())) { // 如果用户不存在或者已注销
            result.put("status","error");
            result.put("msg","用户不存在或已注销");
            return result;
        }
        // 用户存在，获取userId
        Integer userId = user.getId();

        // 判断用户对文章是否进行操作
        // 如果用户没有赞同操作
        result = agreeToArticle(userId,articleId);
        if (!(result.containsValue("error"))){
            result.put("status","error");
            result.put("msg","用户没有进行赞同操作，无法取消赞同");
            return result;
        }

        // 如果用户有赞同操作，那么取消赞同，需要对article的like-1,user_article_like的state = 0
        Integer updateArticleResult = articleMapper.reduceLike(articleId);
        if (updateArticleResult == null || updateArticleResult <= 0){
            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        Integer deleteResult = userArticleLikeMapper.delete(userId,articleId);
        if (deleteResult == null || deleteResult <= 0){
            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        result.put("status","success");
        result.put("msg","数据库操作成功");
        return result;
    }

    /**
     * 取消反对文章
     * 20240920
     * @param email
     * @param articleId
     * @return
     */
    @Transactional
    public Map<String,String> cancelDisagreeArticle(String email,int articleId){

        Map<String,String> result = new HashMap<>();
        // 获取用户
        User user = userMapper.getUserByEmail(email);
        if (user == null || !(user.isState())) { // 如果用户不存在或者已注销
            result.put("status","error");
            result.put("msg","用户不存在或已注销");
            return result;
        }
        // 用户存在，获取userId
        Integer userId = user.getId();

        // 判断用户对文章是否进行操作
        // 如果用户没有反对操作
        result = disagreeToArticle(userId,articleId);
        if (!(result.containsValue("error"))){
            result.put("status","error");
            result.put("msg","用户没有进行反对操作，无法取消反对");
            return result;
        }

        // 如果用户有反对操作，那么取消反对，需要对article的dislike-1,user_article_dislike的state = 0
        Integer updateArticleResult = articleMapper.reduceDislike(articleId);
        if (updateArticleResult == null || updateArticleResult <= 0){
            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        Integer deleteResult = userArticleDislikeMapper.delete(userId,articleId);
        if (deleteResult == null || deleteResult <= 0){
            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        result.put("status","success");
        result.put("msg","数据库操作成功");
        return result;
    }

    // 返回文章点赞人数
    public Map<String,Object> agreeArticleNumber(int articleId){

        Map<String,Object> result = new HashMap<>();
        try {
            // 获取点赞人数
            int like = articleMapper.like(articleId);
            result.put("status","success");
            result.put("like",like);//返回点赞人数
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("数据库操作出现问题:{}",e.getMessage());
            result.put("status","error");
            result.put("msg","数据库操作失败");
        }
        return result;
    }

    // 更新文章内容
    public Map<String,String> updateArticle(Integer articleId,String email, String content){

        Map<String,String> result = new HashMap<>();
        // 参数判空
        if (articleId == null || articleId <= 0){
            result.put("status","error");
            result.put("msg","articleId为空或无效");
            return result;
        }
        if (email == null || email.isEmpty()){
            result.put("status","error");
            result.put("msg","email为空");
            return result;
        }
        if (content == null || content.isEmpty()){
            result.put("status","error");
            result.put("msg","content为空");
            return result;
        }

        // 检查articleId是否存在
        try {
            String articleContent = articleMapper.findContent(articleId);
            if (articleContent == null){
                result.put("status","error");
                result.put("msg","articleId不存在，无法更新");
                return result;
            }
        } catch (Exception e) {
            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        // 回答的作者要和登录用户对上才能修改（以防误触修改其他人文章）
        Integer userId = userMapper.getUserByEmail(email).getId();
        Integer authorId = articleMapper.findAuthor(articleId);
        if (userId != authorId){
            result.put("status","error");
            result.put("msg","非法操作");
            return result;
        }

        // 创建一个article实例
        Article article = new Article();
        article.setArticleId(articleId);
        article.setContent(content);

        // 保存进数据库
        try {
            Integer updateResult = articleMapper.updateArticle(article);
            // 返回保存结果
            if (updateResult == null || updateResult <= 0){
                result.put("status","error");
                result.put("msg","更新文章失败");
                return result;
            }
        } catch (Exception e) {
            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        result.put("status","success");
        return result;
    }

    // 删除文章
    public Map<String,String> updateArticleState(Integer articleId, String email){

        Map<String,String> result = new HashMap<>();
        // 参数判空
        if (articleId == null || articleId <= 0){
            result.put("status","error");
            result.put("msg","articleId为空或无效");
            return result;
        }
        if (email == null || email.isEmpty()){
            result.put("status","error");
            result.put("msg","email为空");
            return result;
        }

        // 检查articleId是否存在
        try {
            String articleContent = articleMapper.findContent(articleId);
            if (articleContent == null){
                result.put("status","error");
                result.put("msg","articleId不存在");
                return result;
            }
        } catch (Exception e) {
            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        // 回答的作者要和登录用户对上才能删除（以防误触删除其他人文章）
        Integer userId = userMapper.getUserByEmail(email).getId();
        Integer authorId = articleMapper.findAuthor(articleId);
        if (userId != authorId){
            result.put("status","error");
            result.put("msg","非法操作");
            return result;
        }

        // 修改文章的状态state
        try {
            Integer updateResult = articleMapper.deleteByArticleId(articleId);
            // 返回保存结果
            if (updateResult == null || updateResult <= 0){
                result.put("status","error");
                result.put("msg","删除文章失败");
                return result;
            }
        } catch (Exception e) {
            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }
        result.put("status","success");
        return result;


    }

    // 显示文章内容
    public Map<String,Object> showArticleContent(int articleId){

        Map<String,Object> result = new HashMap<>();
        try {
            String content = articleMapper.findContent(articleId);
            if (content != null && !(content.isEmpty())){
                result.put("status","success");
                result.put("content",content);
                return result;
            } else {
                result.put("status","error");
                result.put("msg","超出索引范围");
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status","error");
            result.put("msg","从数据库找文章内容失败");
            return result;
        }

    }

}
