package com.lt.questnest.service.Impl;


import com.lt.questnest.controller.UserController;
import com.lt.questnest.entity.History;
import com.lt.questnest.entity.UserTopic;
import com.lt.questnest.mapper.ArticleMapper;
import com.lt.questnest.mapper.HistoryMapper;
import com.lt.questnest.mapper.QuestionMapper;
import com.lt.questnest.mapper.UserMapper;
import com.lt.questnest.service.HistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HistoryServiceImpl implements HistoryService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    ArticleMapper articleMapper;

    @Autowired
    HistoryMapper historyMapper;

    @Autowired
    QuestionMapper questionMapper;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 添加浏览历史记录
    public Map<String, Object> addHistory(String email, Integer articleId) {

        Map<String, Object> result = new HashMap<>();
        // 判断参数是否为空
        if (articleId == null) {
            result.put("status", "error");
            result.put("msg", "传入回答ID为空");
            return result;
        }

        try {
            // 获取用户ID
            Integer userId = userMapper.getUserByEmail(email).getId();

            // 检查articleId是否存在
            Integer questionId = articleMapper.findQuestionId(articleId);
            if (questionId == null) {
                result.put("status", "error");
                result.put("msg", "回答不存在");
                return result;
            }

            // 创建History实例
            History history = new History();
            history.setUserId(userId);
            history.setArticleId(articleId);
            history.setQuestionId(questionId);

            // 保存数据到数据库
            Integer addResult = historyMapper.addHistory(history);
            if (addResult == null || addResult <= 0) {
                result.put("status", "error");
                result.put("msg", "数据保存失败");
                return result;
            }

            // 返回处理结果
            result.put("status", "success");

        } catch (Exception e) {

            result.put("status", "error");
            result.put("msg", "数据库操作异常");
            return result;
        }

        return result;
    }

    // 显示浏览历史记录
    public Map<String, Object> showHistory(String email) {

        Map<String, Object> result = new HashMap<>();

        // 获取用户ID
        Integer userId = userMapper.getUserByEmail(email).getId();
        try {
            // 数据结果列表
            List<Map<String, Object>> historyList = new ArrayList<>();

            // 找出浏览历史记录
            List<History> histories = historyMapper.findByUserId(userId);
            if (histories == null || histories.isEmpty()) {
                result.put("status", "success");
                result.put("msg", "最近7天暂无历史记录");
                return result;
            }

            // 找出问题title和回答content
            for (History history : histories) {

                Map<String, Object> historyItem = new HashMap<>();

                // 获取浏览历史信息，将信息返回给前端，包括historyId、questionId、title、articleId、content、viewTime
                historyItem.put("historyId", history.getHistoryId());
                historyItem.put("questionId", history.getQuestionId());
                String questionTitle = questionMapper.findQuestionTitle(history.getQuestionId());
                historyItem.put("questionTitle", questionTitle);
                historyItem.put("articleId", history.getArticleId());
                String articleContent = articleMapper.findContent(history.getArticleId());
                historyItem.put("articleContent", articleContent);

                // 修改viewTime格式
                Timestamp viewTime = history.getViewTime();  // viewTime:2024-10-13T10:50:52.000+00:00
                // 创建 SimpleDateFormat 实例以定义输出格式
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                // 格式化 Timestamp 为字符串
                String formattedViewTime = outputFormat.format(viewTime);
                historyItem.put("viewTime", formattedViewTime);
                logger.info("修改后的viewTime:{}",formattedViewTime);

                // 添加
                historyList.add(historyItem);
            }

            // 返回话题列表
            result.put("status", "success");
            result.put("history", historyList);

        } catch (Exception e) {

            result.put("status", "error");
            result.put("msg", "数据库操作异常");
            return result;
        }

        return result;
    }

}
