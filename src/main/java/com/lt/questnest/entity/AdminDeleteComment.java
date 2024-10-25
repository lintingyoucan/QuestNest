package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "admin_delete_comment")
public class AdminDeleteComment {

    private Integer adminDeleteCommentId;  // 唯一标识，自增，主键
    private Integer adminId;                 // 管理员ID
    private Integer commentId;               // 评论ID
    private Timestamp deleteTime;        // 删除时间
    private String reason;               // 删除原因
}
