package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "draft")
public class Draft {

    // 草稿唯一标识
    private Integer draftId;

    // 关联用户的ID
    private Integer userId;

    // 关联问题的ID
    private Integer questionId;

    // 草稿正文内容
    private String content;

    // 创建时间
    private Timestamp createTime;

    // 最后修改时间
    private Timestamp updateTime;

    // 是否已经发布
    private boolean post;
}
