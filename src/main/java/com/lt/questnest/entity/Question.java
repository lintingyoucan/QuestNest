package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "question")
public class Question {

    private Integer questionId;
    private Integer userId;
    private String title;
    private String content;
    private Timestamp createTime;
    private Timestamp updateTime;
    private boolean state;  // 审核是否通过

    public Question(){
    }

    public Question(Integer userId, String title, String content){
        this.userId = userId;
        this.title = title;
        this.content = content;
    }

}
