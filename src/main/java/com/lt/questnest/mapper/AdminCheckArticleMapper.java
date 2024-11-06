package com.lt.questnest.mapper;

import com.lt.questnest.entity.AdminCheckArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminCheckArticleMapper {

    List<AdminCheckArticle> get();

    int delete(@Param("articleId") Integer articleId);

}
