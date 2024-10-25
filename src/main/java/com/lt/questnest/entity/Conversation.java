package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "conversation")
public class Conversation {

    private Integer conversationId;    // 会话唯一标识
    private Integer user1Id;           // 用户1的ID
    private Integer user2Id;           // 用户2的ID
    private Integer lastMessageId;     // 最近一条消息的ID
    private Timestamp createTime;  // 会话创建时间
    private Timestamp updateTime;  // 最近一次消息更新时间
    private Integer unreadCount;   // 未读消息数
}
