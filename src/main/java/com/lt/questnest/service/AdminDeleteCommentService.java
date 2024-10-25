package com.lt.questnest.service;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AdminDeleteCommentService {

    Map<String,String> deleteComment(String account, Integer commentId, String reason);
}
