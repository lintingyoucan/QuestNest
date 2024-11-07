package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "report_comment_view")
public class ReportCommentView {

    private Integer reportCommentId;     // 举报评论 ID
    private String reporterUsername; // 举报者用户名
    private String reporterHeadUrl;   // 举报者头像 URL
    private Integer commentId;           // 评论 ID
    private String commentContent;     // 评论内容
    private Integer reporterId;          // 举报者 ID
    private Timestamp reportTime;          // 举报时间
    private String reason;            // 举报原因

}
