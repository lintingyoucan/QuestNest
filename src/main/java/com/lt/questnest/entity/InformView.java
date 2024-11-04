package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "inform_view")
public class InformView {

    private String senderEmail;     // 发送者邮箱
    private String senderUsername;   // 发送者用户名
    private String receiverEmail;    // 接收者邮箱
    private String content;          // 消息内容
    private Timestamp sendTime;      // 发送时间
}
