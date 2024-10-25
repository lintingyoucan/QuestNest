package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "user_article_dislike")
public class UserArticleDislike {
    private Integer dislikeId;        // 行为记录的唯一标识
    private Integer userId;        // 操作的用户ID
    private Integer articleId;     // 操作的文章ID
    private Timestamp createTime;  // 操作时间
}
