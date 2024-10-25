package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "comment")
public class Comment {

    private Integer commentId;      // 评论ID
    private Integer articleId;      // 对应的文章
    private Integer userId;         // 评论的用户
    private Integer parentCommentId; // 上一级评论（可为空）
    private String content;     // 评论内容
    private Integer like;           // 赞同数
    private Timestamp createTime; // 创建时间

}
