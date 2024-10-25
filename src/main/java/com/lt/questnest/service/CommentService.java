package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface CommentService {

    Map<String,Object> addComment(String email,Integer articleId,Integer parentCommentId,String content);

    Map<String,String> agreeComment(String email,int commentId);

    Map<String,String> cancelAgreeComment(String email,int commentId);

    Map<String,Object> agreeCommentNumber(int commentId);

    Map<String,String> deleteComment(Integer commentId,String email);

}
