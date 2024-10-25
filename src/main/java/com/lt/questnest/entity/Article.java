package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "article")
public class Article {

    private Integer articleId;      // 文章ID
    private Integer questionId;     // 对应的问题
    private Integer userId;         // 发表文章的用户
    private String content;     // 文章内容
    private Integer like;           // 赞同数
    private Integer dislike;        // 反对数
    private Timestamp createTime; // 创建时间
    private Timestamp updateTime; // 更新时间
    private boolean state;      // 审核是否通过

    public Article(){}
    public Article(int questionId,int userId,String content){
        this.questionId = questionId;
        this.userId = userId;
        this.content = content;
    }
}
