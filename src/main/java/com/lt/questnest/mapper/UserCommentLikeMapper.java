package com.lt.questnest.mapper;

import com.lt.questnest.entity.UserCommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserCommentLikeMapper {

    UserCommentLike find(@Param("userId") int userId,@Param("commentId") int commentId);

    int add(@Param("userId") int userId,@Param("commentId") int commentId);

    int delete(@Param("userId") int userId,@Param("commentId") int commentId);
}
