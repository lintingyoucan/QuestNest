package com.lt.questnest.service.Impl;

import com.lt.questnest.controller.UserController;
import com.lt.questnest.entity.Draft;
import com.lt.questnest.mapper.DraftMapper;
import com.lt.questnest.mapper.QuestionMapper;
import com.lt.questnest.mapper.UserMapper;
import com.lt.questnest.service.DraftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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

    // 发布草稿
    public Map<String, String> postDraft(Integer draftId) {

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
                result.put("msg", "draftId不存在，无法发布");
                return result;
            }

            // 如果存在，修改草稿状态，保存进数据库
            Integer updateResult = draftMapper.postDraft(draftId);
            if (updateResult == null || updateResult <= 0) {
                result.put("status", "error");
                result.put("msg", "发布草稿失败");
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
            result.put("status", "error");
            result.put("msg", "数据库操作失败");
            return result;
        }

        // 如果存在,返回草稿信息
        result.put("draft",draft);
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
        result.put("drafts",drafts);
        result.put("status", "success");
        return result;

    }
}
