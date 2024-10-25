package com.lt.questnest.mapper;

import com.lt.questnest.entity.AdminDeleteComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminDeleteCommentMapper {

    int add(AdminDeleteComment adminDeleteComment);
}
