package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface CommentAreaMuteService {

    Map<String, Object> closeCommentArea(Integer articleId,String email);

    Map<String, Object> isMute(Integer articleId);

    Map<String, Object> cancelCloseCommentArea(Integer articleId,String email);
}
