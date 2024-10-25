package com.lt.questnest.mapper;

import com.lt.questnest.entity.AdminDeleteQuestion;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminDeleteQuestionMapper {

    int add(AdminDeleteQuestion adminDeleteQuestion);
}
