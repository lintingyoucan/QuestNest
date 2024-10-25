package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "user_topic")
public class UserTopic {

    private Integer userTopicId;      // 主键
    private Integer userId;           // 关注者用户ID
    private Integer topicId;          // 被关注话题ID
    private Timestamp createTime; // 关注时间
}
