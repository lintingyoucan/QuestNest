package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "history")
public class History {

    private Integer historyId;   // 主键
    private Integer userId;      // 关联用户的ID
    private Integer questionId;  // 关联问题的ID
    private Integer articleId;   // 关联文章的ID
    private Timestamp viewTime;   // 浏览时间
}
