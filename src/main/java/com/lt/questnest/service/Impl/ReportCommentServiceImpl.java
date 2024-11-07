package com.lt.questnest.service.Impl;

import com.lt.questnest.entity.Comment;
import com.lt.questnest.entity.ReportComment;
import com.lt.questnest.entity.ReportCommentView;
import com.lt.questnest.mapper.CommentMapper;
import com.lt.questnest.mapper.ReportCommentMapper;
import com.lt.questnest.mapper.ReportCommentViewMapper;
import com.lt.questnest.mapper.UserMapper;
import com.lt.questnest.service.ReportCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ReportCommentServiceImpl implements ReportCommentService {

    @Autowired
    CommentMapper commentMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ReportCommentMapper reportCommentMapper;

    @Autowired
    ReportCommentViewMapper reportCommentViewMapper;

    // 添加举报评论信息
    public Map<String,String> reportComment(String email,Integer commentId,String reason){

        Map<String,String> result = new HashMap<>();

        // 判断参数是否为空
        if (commentId == null){
            result.put("status","error");
            result.put("msg","举报的评论不能为空");
            return result;
        }
        if (reason == null || reason.isEmpty()){
            result.put("status","error");
            result.put("msg","举报原因不能为空");
            return result;
        }

        // 检查评论是否存在
        Comment comment = commentMapper.findComment(commentId);
        if (comment == null){
            result.put("status","error");
            result.put("msg","评论不存在");
            return result;
        }

        // 找出用户ID
        Integer userId = userMapper.getUserByEmail(email).getId();

        // 创建实例
        ReportComment reportComment = new ReportComment();
        reportComment.setCommentId(commentId);
        reportComment.setReporterId(userId);
        reportComment.setReason(reason);

        // 保存数据
        Integer add = reportCommentMapper.add(reportComment);
        if (add == null || add <= 0){
            result.put("status","error");
            result.put("msg","保存数据失败");
            return result;
        }

        result.put("status","success");
        return result;
    }


    // 显示举报评论信息
    public Map<String,Object> showReportComment(){

        Map<String,Object> result = new HashMap<>();

        List<ReportCommentView> reportComments = reportCommentViewMapper.getReportComment();
        if (reportComments == null || reportComments.isEmpty()){
            result.put("status","success");
            result.put("reportComment",new ArrayList<>());
            return result;
        }

        // 数据结果
        List<Map<String,Object>> reportCommentList = new ArrayList<>();

        for (ReportCommentView reportComment : reportComments) {
            Map<String,Object> reportCommentItem = new HashMap<>();
            reportCommentItem.put("reportCommentId",reportComment.getReportCommentId());
            reportCommentItem.put("reporterId",reportComment.getReporterId());
            reportCommentItem.put("reporterUsername",reportComment.getReporterUsername());
            reportCommentItem.put("commentId",reportComment.getCommentId());
            reportCommentItem.put("commentContent",reportComment.getCommentContent());
            reportCommentItem.put("reason",reportComment.getReason());
            // 格式化时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String reportTime = sdf.format(reportComment.getReportTime());
            reportCommentItem.put("reportTime",reportTime);

            reportCommentList.add(reportCommentItem);
        }

        result.put("reportComment",reportCommentList);
        result.put("status","success");
        return result;

    }

    // 处理举报信息后，删除举报信息
    public Map<String,Object> deleteReportComment(Integer reportCommentId){

        Map<String,Object> result = new HashMap<>();
        if (reportCommentId == null){
            result.put("status","error");
            result.put("msg","举报信息ID不能为空");
            return result;
        }

        Integer delete = reportCommentMapper.delete(reportCommentId);
        if (delete == null || delete <= 0){
            result.put("status","error");
            result.put("msg","删除举报信息失败");
            return result;
        }

        result.put("status","success");
        return result;

    }
}
