package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "favourite")
public class Favourite {
    private Integer favouriteId;        // 收藏夹ID
    private Integer userId;             // 用户ID
    private String name;            // 收藏夹名称
    private Timestamp createTime;   // 创建时间
}
