package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "favourite_item")
public class FavouriteItem {

    private Integer favouriteItemId;  // 收藏项ID
    private Integer favouriteId;       // 收藏夹ID
    private Integer articleId;         // 文章ID
    private Timestamp createTime;  // 创建时间
}
