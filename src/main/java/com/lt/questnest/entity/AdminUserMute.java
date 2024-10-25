package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "admin_user_mute")
public class AdminUserMute {

    private Integer adminUserMuteId;  // 唯一标识，自增，主键
    private Integer adminId;           // 管理员ID
    private Integer userId;            // 用户ID
    private Timestamp muteTime;    // 禁言时间
    private String reason;         // 禁言原因
}
