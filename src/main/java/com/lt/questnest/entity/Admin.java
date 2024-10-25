package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "admin")
public class Admin {

    private Integer adminId;          // 唯一标识，自增，主键
    private String account;       // 账号，不为空，唯一
    private String username;      // 用户名，不为空
    private String password;      // 密码，不为空
    private String gender;        // 性别，'男' 或 '女'
    private String headUrl;       // 头像 URL
    private boolean state;        // 状态，默认为1表示用户存在
}
