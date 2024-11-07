package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface ReportCommentService {

    Map<String,String> reportComment(String email, Integer commentId, String reason);

    Map<String,Object> showReportComment();

    Map<String,Object> deleteReportComment(Integer reportCommentId);
}
