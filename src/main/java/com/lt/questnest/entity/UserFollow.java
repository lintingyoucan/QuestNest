package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "user_follow")
public class UserFollow {

    private Integer userFollowId;  // 主键
    private Integer followerId;     // 关注者用户ID
    private Integer followedId;     // 被关注者用户ID
    private Timestamp createTime; // 关注时间
    private boolean state;             // 是否存在
}
