package com.lt.questnest.mapper;

import com.lt.questnest.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {

    int addComment(Comment comment);

    int addLike(@Param("commentId") int commentId);

    int reduceLike(@Param("commentId") int commentId);

    int like(@Param("commentId") int commentId);

    Comment findByParentCommentId(@Param("parentCommentId") int parentCommentId);

    Comment findComment(@Param("commentId") int commentId);

    int deleteByCommentId(@Param("commentId") int commentId);
}
