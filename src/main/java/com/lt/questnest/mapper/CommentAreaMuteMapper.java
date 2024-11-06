package com.lt.questnest.mapper;

import com.lt.questnest.entity.CommentAreaMute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentAreaMuteMapper {

    Integer findByArticleId(@Param("articleId") Integer articleId);

    int add(CommentAreaMute adminCommentAreaMute);

    int delete(@Param("articleId") Integer articleId);
}
