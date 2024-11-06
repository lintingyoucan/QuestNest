package com.lt.questnest.service.Impl;

import com.lt.questnest.entity.AdminCheckQuestion;
import com.lt.questnest.mapper.AdminCheckQuestionMapper;
import com.lt.questnest.mapper.QuestionMapper;
import com.lt.questnest.service.AdminCheckQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminCheckQuestionServiceImpl implements AdminCheckQuestionService {

    @Autowired
    AdminCheckQuestionMapper adminCheckQuestionMapper;

    @Autowired
    QuestionMapper questionMapper;

    // 获取未审核问题
    public Map<String,Object> getQuestion(){

        Map<String,Object> result = new HashMap<>();
        // 获取未审核的问题
        List<AdminCheckQuestion> questions = adminCheckQuestionMapper.get();
        if (questions == null || questions.isEmpty()){
            result.put("status","success");
            result.put("question",new ArrayList<>());
            return result;
        }
        List<Map<String,Object>> questionList = new ArrayList<>();
        for (AdminCheckQuestion adminCheckQuestion : questions) {
            Map<String,Object> questionItem = new HashMap<>();
            questionItem.put("questionId",adminCheckQuestion.getQuestionId());
            // 找出问题title
            String title = questionMapper.findByQuestionId(adminCheckQuestion.getQuestionId()).getTitle();
            questionItem.put("title",title);

            questionList.add(questionItem);
        }

        result.put("question",questionList);
        result.put("status","success");
        return result;
    }
}
