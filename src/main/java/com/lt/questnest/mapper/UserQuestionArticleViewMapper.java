package com.lt.questnest.mapper;

import com.lt.questnest.entity.UserQuestionArticleView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserQuestionArticleViewMapper {

    List<UserQuestionArticleView> get(@Param("questionId") Integer questionId);
}
