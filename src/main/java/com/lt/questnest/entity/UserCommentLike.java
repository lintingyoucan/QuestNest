package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "user_comment_like")
public class UserCommentLike {

    private Integer likeId;
    private Integer userId;
    private Integer commentId;
    private Timestamp createTime;

}
