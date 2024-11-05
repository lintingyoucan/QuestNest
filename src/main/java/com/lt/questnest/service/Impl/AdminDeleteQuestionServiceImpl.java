package com.lt.questnest.service.Impl;

import com.lt.questnest.entity.AdminDeleteQuestion;
import com.lt.questnest.entity.Question;
import com.lt.questnest.mapper.AdminDeleteQuestionMapper;
import com.lt.questnest.mapper.AdminMapper;
import com.lt.questnest.mapper.QuestionMapper;
import com.lt.questnest.mapper.UserMapper;
import com.lt.questnest.pubsub.RedisMessagePublisher;
import com.lt.questnest.service.AdminDeleteQuestionService;
import com.lt.questnest.service.InformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminDeleteQuestionServiceImpl implements AdminDeleteQuestionService {

    @Autowired
    AdminMapper adminMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    AdminDeleteQuestionMapper adminDeleteQuestionMapper;

    @Autowired
    QuestionMapper questionMapper;

    @Autowired
    RedisMessagePublisher redisMessagePublisher;

    @Autowired
    InformService informService;

    @Transactional
    public Map<String,String> failToQuestion(String account, Integer questionId, String reason){

        Map<String,String> result = new HashMap<>();
        // 参数判空
        if (questionId == null || questionId <= 0){
            result.put("status","error");
            result.put("msg","questionId为空或无效");
            return result;
        }
        if (reason == null || reason.isEmpty()){
            result.put("status","error");
            result.put("msg","原因不能为空");
            return result;
        }


        String email; // 问题作者的email
        String title; // 问题title
        Integer userId; // 问题作者的ID

        // 修改问题状态、添加记录
        try {

            // 判断articleId是否存在和审核是否通过（如果state为0，说明审核不通过，不用再添加记录）
            Question question = questionMapper.findByQuestionId(questionId);
            if (question == null){
                result.put("status","error");
                result.put("msg","问题不存在或审核已经不给通过,无法打回重修");
                return result;
            }

            title = question.getTitle();

            // 获取问题作者
            userId = question.getUserId();
            email = userMapper.getUserById(userId).getEmail();

            // 找出管理员ID
            Integer adminId = adminMapper.findByAccount(account).getAdminId();
            // 创建实例
            AdminDeleteQuestion adminDeleteQuestion = new AdminDeleteQuestion();
            adminDeleteQuestion.setAdminId(adminId);
            adminDeleteQuestion.setQuestionId(questionId);
            adminDeleteQuestion.setReason(reason);

            Integer addResult = adminDeleteQuestionMapper.add(adminDeleteQuestion);
            if (addResult == null || addResult <= 0){
                throw new RuntimeException("添加记录失败，问题无法打回重修");
            }

            Integer updateResult = questionMapper.updateQuestionState(questionId);
            if (updateResult == null || updateResult <= 0){
                throw new RuntimeException("问题打回重修失败");
            }

        } catch (Exception e) {
            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        // 异步通知作者重修回答，给出违规原因
        // 检查用户是否存在，如果用户注销，那么不必通知,避免添加数据时出现null错误
        if (email == null || email.isEmpty()){
            result.put("status", "success");
            return result;
        }

        String body = "{\"reason\":\"" + reason + "\", \"title\":\"" + title + "\", \"message\":\"请重新编辑\"}";
        String save = informService.add(0,userId,body);
        String publish = redisMessagePublisher.publish(email,body);
        if (save.equals("success") && publish.equals("success")) {
            result.put("status", "success");
            return result;
        }
        throw new RuntimeException("异步站内通知失败");
    }
}
