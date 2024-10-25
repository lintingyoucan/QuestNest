package com.lt.questnest.mapper;

import com.lt.questnest.entity.QuestionTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionTopicMapper {

    // 是否存在关系
    QuestionTopic findByBoth(QuestionTopic questionTopic);

    // 添加关系
    int add(QuestionTopic questionTopic);

    int updateState(QuestionTopic questionTopic);

    List<Integer> findTopicByQuestionId(@Param("questionId") Integer questionId);
}
