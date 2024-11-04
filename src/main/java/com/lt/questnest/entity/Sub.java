package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "sub")
public class Sub {

    private Integer subId;      // 唯一标识，自增，主键
    private String channel;      // 邮箱，不为空，唯一
}
