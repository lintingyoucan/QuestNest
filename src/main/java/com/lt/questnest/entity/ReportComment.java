package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "report_comment")
public class ReportComment {
    private Integer reportCommentId; // 举报评论的ID
    private Integer reporterId;      // 举报者的用户ID
    private Integer commentId;       // 评论的ID
    private Timestamp reportTime; // 举报时间
    private String reason;        // 举报原因
    private boolean read;         // 是否查看
}
