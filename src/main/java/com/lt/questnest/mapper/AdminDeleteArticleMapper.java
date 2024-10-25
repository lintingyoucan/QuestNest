package com.lt.questnest.mapper;

import com.lt.questnest.entity.AdminDeleteArticle;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminDeleteArticleMapper {

    int add(AdminDeleteArticle adminDeleteArticle);
}
