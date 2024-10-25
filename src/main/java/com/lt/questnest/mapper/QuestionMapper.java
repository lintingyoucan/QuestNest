package com.lt.questnest.mapper;

import com.lt.questnest.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionMapper {

    // 添加问题
    int addQuestion(Question question);

    // 获取问题title
    Question findByTitle(@Param("title") String title);

    // 获取所有问题
    List<Question> findAllQuestion();

    // 找title
    String findQuestionTitle(@Param("questionId") int questionId);

    Question findByQuestionId(@Param("questionId") int questionId);

    int updateQuestion(Question question);

    int updateQuestionContent(@Param("questionId") int questionId,@Param("content") String content);

    int updateQuestionState(@Param("questionId") int questionId);
}
