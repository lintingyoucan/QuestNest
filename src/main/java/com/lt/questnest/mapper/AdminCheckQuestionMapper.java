package com.lt.questnest.mapper;

import com.lt.questnest.entity.AdminCheckQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminCheckQuestionMapper {

    List<AdminCheckQuestion> get ();

    int delete(@Param("questionId") Integer questionId);
}