package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "message")
public class Message {

    private Integer messageId;         // 消息唯一标识
    private Integer senderId;          // 发送者用户ID
    private Integer receiverId;        // 接收者用户ID
    private Integer conversationId;    // 会话ID
    private String content;        // 消息内容
    private Timestamp createTime;  // 消息发送时间
    private boolean read;       // 信息阅读状态，默认0
}
