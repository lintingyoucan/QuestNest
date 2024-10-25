package com.lt.questnest.mapper;

import com.lt.questnest.entity.UserArticleLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserArticleLikeMapper {

    int add(@Param("userId") int userId,@Param("articleId") int articleId);

    UserArticleLike find(@Param("userId") int userId, @Param("articleId") int articleId);

    int delete(@Param("userId") int userId,@Param("articleId") int articleId);

}
