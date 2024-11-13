package com.lt.questnest.service.Impl;

import com.lt.questnest.controller.UserController;
import com.lt.questnest.entity.Article;
import com.lt.questnest.entity.Draft;
import com.lt.questnest.mapper.ArticleMapper;
import com.lt.questnest.mapper.DraftMapper;
import com.lt.questnest.mapper.QuestionMapper;
import com.lt.questnest.mapper.UserMapper;
import com.lt.questnest.service.DraftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DraftServiceImpl implements DraftService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    QuestionMapper questionMapper;

    @Autowired
    DraftMapper draftMapper;

    @Autowired
    ArticleMapper articleMapper;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    // 保存草稿
    public Map<String, Object> saveDraft(String email, String title, String content) {

        Map<String, Object> result = new HashMap<>();
        // 判断参数是否为空
        if (title == null || title.isEmpty()) {
            result.put("status", "error");
            result.put("msg", "title不能为空");
            return result;
        }
        if (content == null || content.isEmpty()) {
            result.put("status", "error");
            result.put("msg", "content不能为空");
            return result;
        }

        try {
            // 获取草稿的作者
            Integer userId = userMapper.getUserByEmail(email).getId();
            // 获取问题ID
            Integer questionId = questionMapper.findByTitle(title).getQuestionId();
            if (questionId == null || questionId <= 0){
                result.put("status", "error");
                result.put("msg", "问题不存在");
                return result;
            }

            // 创建Draft实例
            Draft draft = new Draft();
            draft.setUserId(userId);
            draft.setQuestionId(questionId);
            draft.setContent(content);

            // 保存数据到数据库
            Integer addDraftResult = draftMapper.addDraft(draft);
            if (addDraftResult == null || addDraftResult <= 0) {
                result.put("status", "error");
                result.put("msg", "数据保存失败");
                return result;
            }
            // 将添加后草稿ID返回
            int draftId = draft.getDraftId();
            result.put("draftId", draftId);

        } catch (Exception e) {

            result.put("status", "error");
            result.put("msg", "数据库操作异常");
            return result;
        }

        result.put("status", "success");
        return result;
    }

    // 更新草稿
    public Map<String, String> updateDraft(Integer draftId, String content) {

        Map<String, String> result = new HashMap<>();
        // 参数判空
        if (draftId == null || draftId <= 0) {
            result.put("status", "error");
            result.put("msg", "draftId为空或无效");
            return result;
        }
        if (content == null || content.isEmpty()) {
            result.put("status", "error");
            result.put("msg", "content为空");
            return result;
        }

        try {
            // 检查draftId是否存在
            Draft draft = draftMapper.findByDraftId(draftId);
            if (draft == null) {
                result.put("status", "error");
                result.put("msg", "draftId不存在，无法更新");
                return result;
            }

            draft.setContent(content);
            logger.info("更新后,未保存进数据库前的draft:{}",draft);
            // 保存进数据库
            Integer updateResult = draftMapper.updateDraft(draft);
            // 返回保存结果
            if (updateResult == null || updateResult <= 0) {
                result.put("status", "error");
                result.put("msg", "更新草稿失败");
                return result;
            }

        } catch (Exception e) {

            result.put("status", "error");
            result.put("msg", "数据库操作失败");
            return result;
        }

        result.put("status", "success");
        return result;
    }

    // 发布草稿,有bug：发布草稿，相当于添加回答，所以也要有添加回答的数据库操作
    @Transactional
    public Map<String, Object> postDraft(Integer draftId) {

        Map<String, Object> result = new HashMap<>();
        // 参数判空
        if (draftId == null || draftId <= 0) {
            result.put("status", "error");
            result.put("msg", "draftId为空或无效");
            return result;
        }

        try {
            // 检查draftId是否存在
            Draft draft = draftMapper.findByDraftId(draftId);
            if (draft == null) {
                result.put("status", "error");
                result.put("msg", "draftId不存在，无法发布");
                return result;
            }

            // 如果存在，修改草稿状态，保存进数据库
            Integer updateResult = draftMapper.postDraft(draftId);
            if (updateResult == null || updateResult <= 0) {
                throw new RuntimeException("发布草稿失败！");
            }

            // 添加回答
            Article article = new Article();
            article.setQuestionId(draft.getQuestionId());
            article.setContent(draft.getContent());
            article.setUserId(draft.getUserId());

            Integer addResult = articleMapper.addArticle(article);
            if (addResult == null || addResult <= 0){
                throw new RuntimeException("发布草稿时。添加回答失败！");
            }
            // 返回添加后的文章ID
            Integer articleId = article.getArticleId();
            result.put("articleId",articleId);

        } catch (Exception e) {
            throw new RuntimeException("根据draftId访问数据库失败！");
        }

        result.put("status", "success");
        return result;

    }

    // 删除草稿
    public Map<String, String> deleteDraft(Integer draftId) {

        Map<String, String> result = new HashMap<>();
        // 参数判空
        if (draftId == null || draftId <= 0) {
            result.put("status", "error");
            result.put("msg", "draftId为空或无效");
            return result;
        }

        try {
            // 检查draftId是否存在
            Draft draft = draftMapper.findByDraftId(draftId);
            if (draft == null) {
                result.put("status", "error");
                result.put("msg", "draftId不存在");
                return result;
            }
            // 如果存在，将草稿状态state改为0，不存在
            Integer updateResult = draftMapper.deleteDraftByDraftId(draftId);
            if (updateResult == null || updateResult <= 0) {
                result.put("status", "error");
                result.put("msg", "删除草稿失败");
                return result;
            }

        } catch (Exception e) {
            result.put("status", "error");
            result.put("msg", "数据库操作失败");
            return result;
        }

        result.put("status", "success");
        return result;

    }

    // 根据draftId显示草稿
    public Map<String,Object> showDraftByDraftId(Integer draftId) {

        Map<String, Object> result = new HashMap<>();
        // 参数判空
        if (draftId == null || draftId <= 0) {
            result.put("status", "error");
            result.put("msg", "draftId为空或无效");
            return result;
        }

        Draft draft = null;
        try {
            // 检查draftId是否存在
            draft = draftMapper.findByDraftId(draftId);
            if (draft == null) {
                result.put("status", "error");
                result.put("msg", "草稿不存在");
                return result;
            }

        } catch (Exception e) {
            throw new RuntimeException("根据draftId访问数据库失败！");
        }

        // 如果存在,返回草稿信息
        Map<String,Object> draftItem = new HashMap<>();
        // 草稿ID、对应问题title、更新时间、草稿内容
        draftItem.put("draftId",draft.getDraftId());

        String title = questionMapper.findQuestionTitle(draft.getQuestionId());
        draftItem.put("questionTitle",title);

        Timestamp lastTimeDate = draft.getUpdateTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        // 格式化 Timestamp 对象，转换为字符串
        String lastTime = sdf.format(lastTimeDate);
        draftItem.put("lastTime",lastTime);

        draftItem.put("content",draft.getContent());

        result.put("draft",draftItem);
        result.put("status", "success");
        return result;

    }


    // 根据用户显示草稿
    public Map<String,Object> showDraftByUserId(String email) {

        Map<String, Object> result = new HashMap<>();


        List<Draft> drafts = null;
        try {
            // 找出用户ID
            Integer userId = userMapper.getUserByEmail(email).getId();

            // 检查draftId是否存在
            drafts = draftMapper.findByUserId(userId);
            if (drafts == null || drafts.isEmpty()) {
                result.put("status", "error");
                result.put("msg", "没有草稿");
                return result;
            }

        } catch (Exception e) {
            result.put("status", "error");
            result.put("msg", "数据库操作失败");
            return result;
        }

        // 如果存在,返回草稿信息
        List<Map<String,Object>> draftList = new ArrayList<>();
        for (Draft draft : drafts) {
            Map<String,Object> draftItem = new HashMap<>();
            // 草稿ID、对应问题title、更新时间、草稿内容
            draftItem.put("draftId",draft.getDraftId());

            String title = questionMapper.findQuestionTitle(draft.getQuestionId());
            draftItem.put("questionTitle",title);

            Timestamp lastTimeDate = draft.getUpdateTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            // 格式化 Timestamp 对象，转换为字符串
            String lastTime = sdf.format(lastTimeDate);
            draftItem.put("lastTime",lastTime);

            draftItem.put("content",draft.getContent());

            draftList.add(draftItem);
        }

        result.put("drafts",draftList);
        result.put("status", "success");
        return result;

    }
}
