package com.lt.questnest.service.Impl;

import com.lt.questnest.entity.AdminDeleteComment;
import com.lt.questnest.entity.Comment;
import com.lt.questnest.mapper.AdminDeleteCommentMapper;
import com.lt.questnest.mapper.AdminMapper;
import com.lt.questnest.mapper.CommentMapper;
import com.lt.questnest.service.AdminDeleteCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;


@Service
public class AdminDeleteCommentServiceImpl implements AdminDeleteCommentService {

    @Autowired
    CommentMapper commentMapper;

    @Autowired
    AdminMapper adminMapper;

    @Autowired
    AdminDeleteCommentMapper adminDeleteCommentMapper;

    // 删除评论
    @Transactional
    public Map<String,String> deleteComment(String account,Integer commentId, String reason){

        Map<String,String> result = new HashMap<>();
        // 参数判空
        if (commentId == null || commentId <= 0){
            result.put("status","error");
            result.put("msg","commentId为空或无效");
            return result;
        }
        if (reason == null || reason.isEmpty()){
            result.put("status","error");
            result.put("msg","原因不能为空");
            return result;
        }


        // 判断commentId是否存在
        Comment comment = commentMapper.findComment(commentId);
        if (comment == null){
            result.put("status","error");
            result.put("msg","comment不存在,无法删除");
            return result;
        }

        // 删除评论、添加记录
        try {

            // 找出管理员ID
            Integer adminId = adminMapper.findByAccount(account).getAdminId();
            // 创建实例
            AdminDeleteComment adminDeleteComment = new AdminDeleteComment();
            adminDeleteComment.setCommentId(commentId);
            adminDeleteComment.setAdminId(adminId);
            adminDeleteComment.setReason(reason);

            Integer addResult = adminDeleteCommentMapper.add(adminDeleteComment);
            if (addResult == null || addResult <= 0){
                result.put("status","error");
                result.put("msg","添加记录失败，删除评论失败");
                return result;
            }

            Integer deleteResult = commentMapper.deleteByCommentId(commentId);
            if (deleteResult == null || deleteResult <= 0){
                result.put("status","error");
                result.put("msg","删除评论失败");
                return result;
            }

        } catch (Exception e) {
            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        result.put("status","success");
        return result;
    }
}
