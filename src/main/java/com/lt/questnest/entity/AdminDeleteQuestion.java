package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "admin_delete_question")
public class AdminDeleteQuestion {

    private Integer adminDeleteQuestionId;  // 唯一标识，自增，主键
    private Integer adminId;                 // 管理员ID
    private Integer questionId;              // 问题ID
    private Timestamp deleteTime;        // 删除时间
    private String reason;               // 删除原因
}
